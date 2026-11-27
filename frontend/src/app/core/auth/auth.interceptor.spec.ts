import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { vi } from 'vitest';
import { NotificationService } from '../ui/notification.service';
import { authInterceptor } from './auth.interceptor';
import { AUTH_STORAGE, AuthService } from './auth.service';
import { authResponse, memoryStorage } from './auth.testing';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let auth: AuthService;
  let notifications: NotificationService;

  function logIn() {
    auth.login({ email: 'a@b.c', password: 'x' }).subscribe();
    controller.expectOne('/api/auth/login').flush(authResponse('PATIENT'));
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AUTH_STORAGE, useValue: memoryStorage() },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
    notifications = TestBed.inject(NotificationService);
  });

  afterEach(() => controller.verify());

  it('adds the Bearer token to calls to our API', () => {
    logIn();

    http.get('/api/patients').subscribe();

    expect(controller.expectOne('/api/patients').request.headers.get('Authorization')).toBe(
      'Bearer jwt-token',
    );
  });

  it('never sends the token to other hosts', () => {
    logIn();

    http.get('https://example.com/data').subscribe();

    expect(controller.expectOne('https://example.com/data').request.headers.has('Authorization')).toBe(
      false,
    );
  });

  it('sends no header when logged out', () => {
    http.get('/api/specialties').subscribe();

    expect(controller.expectOne('/api/specialties').request.headers.has('Authorization')).toBe(false);
  });

  it('ends the session on 401 from an authenticated call', () => {
    logIn();
    const logout = vi.spyOn(auth, 'logout').mockImplementation(() => undefined);

    http.get('/api/appointments/me').subscribe({ error: () => undefined });
    controller
      .expectOne('/api/appointments/me')
      .flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(logout).toHaveBeenCalledWith('expired');
  });

  it('treats 401 on the login form as a wrong password, not an expired session', () => {
    const logout = vi.spyOn(auth, 'logout');

    auth.login({ email: 'a@b.c', password: 'wrong' }).subscribe({ error: () => undefined });
    controller.expectOne('/api/auth/login').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(logout).not.toHaveBeenCalled();
  });

  it('warns the user on 403 and keeps the session', () => {
    logIn();
    const error = vi.spyOn(notifications, 'error');

    http.get('/api/patients').subscribe({ error: () => undefined });
    controller.expectOne('/api/patients').flush({}, { status: 403, statusText: 'Forbidden' });

    expect(error).toHaveBeenCalled();
    expect(auth.isAuthenticated()).toBe(true);
  });
});
