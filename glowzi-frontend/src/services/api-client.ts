import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { Platform } from 'react-native';

import { tokenStorage } from './token-storage';
import type { TokenResponse } from '@/types/auth';

// Android emulator uses 10.0.2.2 to reach host machine's localhost.
// iOS simulator and web can use localhost directly.
function getBaseUrl(): string {
  if (!__DEV__) return 'https://api.glowzi.com';

  if (Platform.OS === 'android') return 'http://10.0.2.2:8081';
  return 'http://localhost:8081';
}

const BASE_URL = getBaseUrl();

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' },
});

// Attach access token to every request
apiClient.interceptors.request.use(async (config) => {
  const token = await tokenStorage.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Refresh token on 401 and retry the failed request once
let refreshPromise: Promise<TokenResponse> | null = null;

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status !== 401 || original._retry) {
      return Promise.reject(error);
    }

    // Don't retry auth endpoints themselves
    if (original.url?.includes('/auth/refresh') || original.url?.includes('/auth/login')) {
      return Promise.reject(error);
    }

    original._retry = true;

    try {
      // Coalesce concurrent refresh calls into one request
      if (!refreshPromise) {
        refreshPromise = (async () => {
          const refreshToken = await tokenStorage.getRefreshToken();
          if (!refreshToken) throw new Error('No refresh token');

          const { data } = await axios.post<TokenResponse>(
            `${BASE_URL}/auth/refresh`,
            { refreshToken },
            { headers: { 'Content-Type': 'application/json' } },
          );
          await tokenStorage.setTokens(data.accessToken, data.refreshToken);
          return data;
        })();
      }

      const tokens = await refreshPromise;
      original.headers.Authorization = `Bearer ${tokens.accessToken}`;
      return apiClient(original);
    } catch {
      await tokenStorage.clearTokens();
      return Promise.reject(error);
    } finally {
      refreshPromise = null;
    }
  },
);
