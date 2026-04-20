import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';

import { authService } from '@/services/auth-service';
import { tokenStorage } from '@/services/token-storage';
import type {
  AuthResponse,
  ChangePasswordRequest,
  LoginRequest,
  RegisterRequest,
  UserProfile,
} from '@/types/auth';

interface AuthState {
  user: UserProfile | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

interface AuthContextValue extends AuthState {
  login: (request: LoginRequest) => Promise<AuthResponse>;
  register: (request: RegisterRequest) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  changePassword: (request: ChangePasswordRequest) => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({
    user: null,
    isLoading: true,
    isAuthenticated: false,
  });

  // Try to restore session on mount
  useEffect(() => {
    (async () => {
      try {
        const token = await tokenStorage.getAccessToken();
        if (!token) {
          setState({ user: null, isLoading: false, isAuthenticated: false });
          return;
        }
        const profile = await authService.getProfile();
        setState({ user: profile, isLoading: false, isAuthenticated: true });
      } catch {
        await tokenStorage.clearTokens();
        setState({ user: null, isLoading: false, isAuthenticated: false });
      }
    })();
  }, []);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authService.login(request);
    const profile = await authService.getProfile();
    setState({ user: profile, isLoading: false, isAuthenticated: true });
    return response;
  }, []);

  const register = useCallback(async (request: RegisterRequest) => {
    const response = await authService.register(request);
    const profile = await authService.getProfile();
    setState({ user: profile, isLoading: false, isAuthenticated: true });
    return response;
  }, []);

  const logout = useCallback(async () => {
    await authService.logout();
    setState({ user: null, isLoading: false, isAuthenticated: false });
  }, []);

  const changePassword = useCallback(async (request: ChangePasswordRequest) => {
    await authService.changePassword(request);
  }, []);

  const refreshProfile = useCallback(async () => {
    const profile = await authService.getProfile();
    setState((prev) => ({ ...prev, user: profile }));
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ ...state, login, register, logout, changePassword, refreshProfile }),
    [state, login, register, logout, changePassword, refreshProfile],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
