import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { AUTH_STORAGE, AuthService } from './auth.service';
import { authResponse, memoryStorage } from './auth.testing';

const STORAGE_KEY = 'medagenda.session';

function setup(storage: Storage = memoryStorage()) {
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      provideHttpClient(),
      provideHttpClientTesting(),
      { provide: AUTH_STORAGE, useValue: storage },
    ],
  });
  return {
    auth: TestBed.inject(AuthService),
    http: TestBed.inject(HttpTestingController),
    router: TestBed.inject(Router),
    storage,
  };
}

describe('AuthService', () => {
  afterEach(() => vi.useRealTimers());

  it('starts logged out', () => {
    const { auth } = setup();

    expect(auth.isAuthenticated()).toBe(false);
    expect(auth.user()).toBeNull();
    expect(auth.homeUrl()).toBe('/login');
  });

  it('logs in, exposing user, token and role as signals', () => {
    const { auth, http } = setup();

    auth.login({ email: 'a@b.c', password: 'x' }).subscribe();
    const request = http.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush(authResponse('DOCTOR'));

    expect(auth.isAuthenticated()).toBe(true);
    expect(auth.token()).toBe('jwt-token');
    expect(auth.role()).toBe('DOCTOR');
    expect(auth.hasRole('DOCTOR', 'ADMIN')).toBe(true);
    expect(auth.hasRole('PATIENT')).toBe(false);
  });

  it('persists the session and restores it after a reload', () => {
    const first = setup();
    first.auth.login({ email: 'a@b.c', password: 'x' }).subscribe();
    first.http.expectOne('/api/auth/login').flush(authResponse('PATIENT'));
    TestBed.tick();
    expect(first.storage.getItem(STORAGE_KEY)).toContain('jwt-token');

    TestBed.resetTestingModule();
    const reloaded = setup(first.storage);

    expect(reloaded.auth.isAuthenticated()).toBe(true);
    expect(reloaded.auth.role()).toBe('PATIENT');
  });

  it('ignores corrupted and expired stored sessions', () => {
    expect(setup(memoryStorage({ [STORAGE_KEY]: '{not json' })).auth.isAuthenticated()).toBe(false);

    TestBed.resetTestingModule();
    const expired = JSON.stringify({
      token: 't',
      expiresAt: Date.now() - 1000,
      user: authResponse('PATIENT').user,
    });
    expect(setup(memoryStorage({ [STORAGE_KEY]: expired })).auth.isAuthenticated()).toBe(false);
  });

  it('logs out: clears the session and the storage, then goes to the login page', () => {
    const { auth, http, router, storage } = setup();
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    auth.login({ email: 'a@b.c', password: 'x' }).subscribe();
    http.expectOne('/api/auth/login').flush(authResponse('ADMIN'));
    TestBed.tick();

    auth.logout();
    TestBed.tick();

    expect(auth.isAuthenticated()).toBe(false);
    expect(storage.getItem(STORAGE_KEY)).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: {} });
  });

  it('logs out by itself when the token expires', () => {
    vi.useFakeTimers();
    const { auth, http, router } = setup();
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    auth.login({ email: 'a@b.c', password: 'x' }).subscribe();
    http.expectOne('/api/auth/login').flush(authResponse('PATIENT', 60));
    TestBed.tick();
    expect(auth.isAuthenticated()).toBe(true);

    vi.advanceTimersByTime(61_000);

    expect(auth.isAuthenticated()).toBe(false);
  });

  it('sends each role to its own home page', () => {
    const { auth } = setup();

    expect(auth.homeUrl('PATIENT')).toBe('/paciente');
    expect(auth.homeUrl('DOCTOR')).toBe('/medico');
    expect(auth.homeUrl('RECEPTIONIST')).toBe('/equipe');
    expect(auth.homeUrl('ADMIN')).toBe('/equipe');
  });

  it('shows the first name, keeping the title of doctors', () => {
    const patient = setup();
    patient.auth.login({ email: 'a', password: 'b' }).subscribe();
    patient.http.expectOne('/api/auth/login').flush(authResponse('PATIENT', 7200, 'João Pereira'));
    expect(patient.auth.firstName()).toBe('João');

    TestBed.resetTestingModule();
    const doctor = setup();
    doctor.auth.login({ email: 'a', password: 'b' }).subscribe();
    doctor.http.expectOne('/api/auth/login').flush(authResponse('DOCTOR', 7200, 'Dra. Ana Cardoso'));
    expect(doctor.auth.firstName()).toBe('Dra. Ana');
  });
});
