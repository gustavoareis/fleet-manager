import { HttpClient } from '@angular/common/http';
import { Injectable, InjectionToken, computed, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegisterPatientRequest,
  Role,
  Session,
  UserInfo,
} from './auth.models';

const STORAGE_KEY = 'medagenda.session';

/** Injected (instead of using localStorage directly) so tests can pass an in-memory Storage. */
export const AUTH_STORAGE = new InjectionToken<Storage>('AUTH_STORAGE', {
  providedIn: 'root',
  factory: () => localStorage,
});

export type LogoutReason = 'expired' | 'signed-out';

const HOME_BY_ROLE: Record<Role, string> = {
  PATIENT: '/paciente',
  DOCTOR: '/medico',
  RECEPTIONIST: '/equipe',
  ADMIN: '/equipe',
};

/**
 * Single source of truth for "who is logged in".
 *
 * One writable signal holds the session; everything else is derived with computed(), so
 * components and guards always read a consistent value and re-render only when it changes.
 * Side effects (persisting, auto-logout) live in effect(), reacting to the session signal.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly storage = inject(AUTH_STORAGE);

  private readonly session = signal<Session | null>(this.readStoredSession());

  readonly user = computed<UserInfo | null>(() => this.session()?.user ?? null);
  readonly token = computed<string | null>(() => this.session()?.token ?? null);
  readonly isAuthenticated = computed(() => this.session() !== null);
  readonly role = computed<Role | null>(() => this.user()?.role ?? null);
  /** "João Pereira" -> "João"; "Dra. Ana Cardoso" -> "Dra. Ana" (keeps the title). */
  readonly firstName = computed(() => {
    const words = this.user()?.displayName.trim().split(/\s+/) ?? [];
    const hasTitle = /^dra?\.?$/i.test(words[0] ?? '') && words.length > 1;
    return (hasTitle ? words.slice(0, 2) : words.slice(0, 1)).join(' ');
  });

  constructor() {
    // Persist so a page reload keeps the user logged in
    effect(() => {
      const session = this.session();
      if (session) {
        this.storage.setItem(STORAGE_KEY, JSON.stringify(session));
      } else {
        this.storage.removeItem(STORAGE_KEY);
      }
    });

    // Log out exactly when the token expires; onCleanup cancels the timer if the session changes
    effect((onCleanup) => {
      const session = this.session();
      if (!session) {
        return;
      }
      const timer = setTimeout(() => this.logout('expired'), session.expiresAt - Date.now());
      onCleanup(() => clearTimeout(timer));
    });

    // Logging out (or in) in one tab is reflected in the other open tabs
    globalThis.addEventListener?.('storage', (event: StorageEvent) => {
      if (event.key === STORAGE_KEY) {
        this.session.set(this.readStoredSession());
      }
    });
  }

  login(credentials: LoginRequest): Observable<UserInfo> {
    return this.http
      .post<AuthResponse>('/api/auth/login', credentials)
      .pipe(map((response) => this.startSession(response)));
  }

  /** Patient self sign-up; the backend already returns a token (auto-login). */
  register(request: RegisterPatientRequest): Observable<UserInfo> {
    return this.http
      .post<AuthResponse>('/api/auth/register', request)
      .pipe(map((response) => this.startSession(response)));
  }

  /**
   * Stateless API: logging out is just forgetting the token.
   * The current URL is kept so the user returns to it after signing in again.
   */
  logout(reason: LogoutReason = 'signed-out'): void {
    const returnUrl = reason === 'expired' ? this.router.url : undefined;
    this.session.set(null);
    void this.router.navigate(['/login'], {
      queryParams: reason === 'expired' ? { reason, returnUrl } : {},
    });
  }

  homeUrl(role: Role | null = this.role()): string {
    return role ? HOME_BY_ROLE[role] : '/login';
  }

  hasRole(...roles: Role[]): boolean {
    const role = this.role();
    return role !== null && roles.includes(role);
  }

  private startSession(response: AuthResponse): UserInfo {
    this.session.set({
      token: response.accessToken,
      expiresAt: Date.now() + response.expiresIn * 1000,
      user: response.user,
    });
    return response.user;
  }

  /** Ignores missing, corrupted or already expired data instead of failing on startup. */
  private readStoredSession(): Session | null {
    try {
      const raw = this.storage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }
      const session = JSON.parse(raw) as Session;
      return session.token && session.expiresAt > Date.now() ? session : null;
    } catch {
      return null;
    }
  }
}
