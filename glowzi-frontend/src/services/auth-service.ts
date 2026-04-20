import { apiClient } from './api-client';
import { tokenStorage } from './token-storage';
import type {
  AuthResponse,
  ChangePasswordRequest,
  LoginRequest,
  RegisterRequest,
  TokenResponse,
  UserProfile,
} from '@/types/auth';

export const authService = {
  async register(request: RegisterRequest): Promise<AuthResponse> {
    const { data } = await apiClient.post<AuthResponse>('/auth/register', request);
    await tokenStorage.setTokens(data.accessToken, data.refreshToken);
    return data;
  },

  async login(request: LoginRequest): Promise<AuthResponse> {
    const { data } = await apiClient.post<AuthResponse>('/auth/login', request);
    await tokenStorage.setTokens(data.accessToken, data.refreshToken);
    return data;
  },

  async refresh(): Promise<TokenResponse> {
    const refreshToken = await tokenStorage.getRefreshToken();
    if (!refreshToken) throw new Error('No refresh token available');

    const { data } = await apiClient.post<TokenResponse>('/auth/refresh', { refreshToken });
    await tokenStorage.setTokens(data.accessToken, data.refreshToken);
    return data;
  },

  async getProfile(): Promise<UserProfile> {
    const { data } = await apiClient.get<UserProfile>('/auth/me');
    return data;
  },

  async logout(): Promise<void> {
    const refreshToken = await tokenStorage.getRefreshToken();
    if (refreshToken) {
      try {
        await apiClient.post('/auth/logout', { refreshToken });
      } catch {
        // Best-effort: clear locally even if server call fails
      }
    }
    await tokenStorage.clearTokens();
  },

  async changePassword(request: ChangePasswordRequest): Promise<void> {
    await apiClient.post('/auth/change-password', request);
  },
};
