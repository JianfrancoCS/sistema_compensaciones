export interface LoginRequest {
  username: string;
  password: string;
  platform?: string; // "WEB", "MOBILE", "DESKTOP" - Hardcodeado a "WEB" en el servicio
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  tokenType: string;
  userId: string;
  username: string;
  expiresIn: number;
  refreshExpiresIn: number;
  menu: NavigationItem[];
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface NavigationItem {
  id: string;
  displayName: string;
  icon: string;
  iconUrl?: string | null;
  route?: string | null;
  children: NavigationItem[];
}

