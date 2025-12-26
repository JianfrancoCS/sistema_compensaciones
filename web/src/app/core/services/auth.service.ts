import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '@env/environment';
import { ApiResult } from '@shared/types/api';
import { LoginRequest, LoginResponse, NavigationItem, RefreshTokenRequest } from '@core/models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly MENU_KEY = 'user_menu';
  private readonly USERNAME_KEY = 'username';
  private readonly USER_ID_KEY = 'user_id';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<ApiResult<LoginResponse>> {
    const request: LoginRequest = { username, password, platform: 'WEB' };
    return this.http.post<ApiResult<LoginResponse>>(
      `${environment.apiUrl}/v1/auth/login`,
      request
    ).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.setToken(response.data.token);
          this.setRefreshToken(response.data.refreshToken);
          this.setMenu(response.data.menu);
          this.setUsername(response.data.username);
          this.setUserId(response.data.userId);
        }
      })
    );
  }

  refreshToken(refreshToken: string): Observable<ApiResult<LoginResponse>> {
    const request: RefreshTokenRequest = { refreshToken };
    return this.http.post<ApiResult<LoginResponse>>(
      `${environment.apiUrl}/v1/auth/refresh`,
      request
    ).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.setToken(response.data.token);
          this.setRefreshToken(response.data.refreshToken);
          this.setMenu(response.data.menu);
          this.setUserId(response.data.userId);
        }
      })
    );
  }

  logout(): Observable<ApiResult<void>> {
    return this.http.post<ApiResult<void>>(
      `${environment.apiUrl}/v1/auth/logout`,
      {}
    ).pipe(
      tap(() => {
        this.clearAuth();
      })
    );
  }

  setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  setRefreshToken(refreshToken: string): void {
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  setMenu(menu: NavigationItem[]): void {
    localStorage.setItem(this.MENU_KEY, JSON.stringify(menu));
  }

  getMenu(): NavigationItem[] {
    const menu = localStorage.getItem(this.MENU_KEY);
    return menu ? JSON.parse(menu) : [];
  }

  setUsername(username: string): void {
    localStorage.setItem(this.USERNAME_KEY, username);
  }

  getUsername(): string | null {
    return localStorage.getItem(this.USERNAME_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  setUserId(userId: string): void {
    localStorage.setItem(this.USER_ID_KEY, userId);
  }

  getUserId(): string | null {
    return localStorage.getItem(this.USER_ID_KEY);
  }

  clearAuth(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.MENU_KEY);
    localStorage.removeItem(this.USERNAME_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
  }

  getMyInfo(): Observable<ApiResult<EmployeeMeResponse>> {
    return this.http.get<ApiResult<EmployeeMeResponse>>(`${environment.apiUrl}/v1/auth/me`);
  }

  changePassword(currentPassword: string, newPassword: string): Observable<ApiResult<void>> {
    return this.http.post<ApiResult<void>>(`${environment.apiUrl}/v1/auth/change-password`, {
      currentPassword,
      newPassword
    });
  }
}

export interface EmployeeMeResponse {
  code: string | null;
  documentNumber: string;
  names: string | null;
  paternalLastname: string | null;
  maternalLastname: string | null;
  dateOfBirth: string | null;
  gender: string | null;
  positionName: string | null;
  subsidiaryId: string | null;
  subsidiaryName: string | null;
  photoUrl?: string | null;
  positionSalary?: number | null;
  customSalary?: number | null;
  afpAffiliationNumber?: string | null;
  retirementConceptName?: string | null;
  bankAccountNumber?: string | null;
  bankName?: string | null;
  hireDate?: string | null;
  isForeign?: boolean | null;
  nationality?: string | null;
}