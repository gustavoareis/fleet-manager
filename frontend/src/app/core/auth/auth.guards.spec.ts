import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CanMatchFn, Route, Router, UrlSegment, UrlTree, provideRouter } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './auth.guards';
import { AUTH_STORAGE } from './auth.service';
import { Role } from './auth.models';
import { authResponse, memoryStorage } from './auth.testing';

const STORAGE_KEY = 'medagenda.session';

function runGuard(guard: CanMatchFn, role: Role | null, path = 'paciente/consultas') {
  const stored: Record<string, string> = role
    ? {
        [STORAGE_KEY]: JSON.stringify({
          token: 't',
          expiresAt: Date.now() + 3_600_000,
          user: authResponse(role).user,
        }),
      }
    : {};
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: AUTH_STORAGE, useValue: memoryStorage(stored) },
    ],
  });
  const segments = path.split('/').map((p) => new UrlSegment(p, {}));
  return TestBed.runInInjectionContext(() => guard({} as Route, segments, {} as never));
}

const urlOf = (result: unknown) => TestBed.inject(Router).serializeUrl(result as UrlTree);

describe('auth guards', () => {
  describe('authGuard', () => {
    it('lets logged-in users through', () => {
      expect(runGuard(authGuard, 'PATIENT')).toBe(true);
    });

    it('sends anonymous users to login, remembering where they were going', () => {
      expect(urlOf(runGuard(authGuard, null))).toBe('/login?returnUrl=%2Fpaciente%2Fconsultas');
    });
  });

  describe('roleGuard', () => {
    it('allows a listed role', () => {
      expect(runGuard(roleGuard('RECEPTIONIST', 'ADMIN'), 'ADMIN')).toBe(true);
    });

    it('sends a logged-in user with another role to "access denied"', () => {
      expect(urlOf(runGuard(roleGuard('RECEPTIONIST', 'ADMIN'), 'PATIENT'))).toBe('/acesso-negado');
    });

    it('sends anonymous users to login', () => {
      expect(urlOf(runGuard(roleGuard('DOCTOR'), null))).toBe('/login');
    });
  });

  describe('guestGuard', () => {
    it('lets anonymous users see login and sign-up', () => {
      expect(runGuard(guestGuard, null, 'login')).toBe(true);
    });

    it('sends logged-in users to their home page', () => {
      expect(urlOf(runGuard(guestGuard, 'DOCTOR', 'login'))).toBe('/medico');
    });
  });
});
