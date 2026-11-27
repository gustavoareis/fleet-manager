import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../ui/notification.service';
import { AuthService } from './auth.service';

/**
 * 1. Adds "Authorization: Bearer <token>" to calls to our own API (never to third-party URLs,
 *    which would leak the token).
 * 2. 401 on an authenticated call = token expired/revoked: end the session and go to login.
 *    (A 401 from the login form itself is just a wrong password, handled by the form.)
 * 3. 403 = logged in but not allowed: tell the user and keep them on the page.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  // inject() only works here, in the interceptor's injection context, not inside catchError
  const auth = inject(AuthService);
  const notifications = inject(NotificationService);

  const token = auth.token();
  const isApiCall = request.url.startsWith('/api/');
  const authorized =
    token && isApiCall
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;

  return next(authorized).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && isApiCall) {
        if (error.status === 401 && token) {
          auth.logout('expired');
        } else if (error.status === 403) {
          notifications.error('Você não tem permissão para realizar esta operação.');
        }
      }
      return throwError(() => error);
    }),
  );
};
