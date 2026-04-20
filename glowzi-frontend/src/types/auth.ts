export type UserRole = 'CUSTOMER' | 'PROVIDER' | 'ADMIN';

// --- Request DTOs ---

export interface RegisterRequest {
  fullName: string;
  phone: string;
  password: string;
  role: UserRole;
  preferredLanguage?: string;
}

export interface LoginRequest {
  phone: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  refreshToken: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// --- Response DTOs ---

export interface AuthResponse {
  userId: number;
  role: UserRole;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

export interface UserProfile {
  userId: number;
  fullName: string;
  phone: string;
  role: UserRole;
  preferredLanguage?: string;
}

export interface ValidateResponse {
  userId: number;
  role: UserRole;
  phone: string;
}

// --- Error DTOs ---

export interface ApiValidationError {
  error: string;
  fields?: Record<string, string>;
}

export interface ApiError {
  error: string;
}
