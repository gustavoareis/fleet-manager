import { inject } from '@angular/core';
import { CanMatchFn, Router, UrlSegment } from '@angular/router';
import { Role } from './auth.models';
import { AuthService } from './auth.service';

/*
 * canMatch (instead of canActivate) is checked before a lazy route is loaded, so a user
 * without access never even downloads that feature's JavaScript.
 *
 * Guards are UX, not security: the backend enforces every rule again on each request.
 */

function toUrl(segments: UrlSegment[]): string {
  return '/' + segments.map((segment) => segment.path).join('/');
}

/** Only logged-in users; others go to the login page and come back afterwards. */
export const authGuard: CanMatchFn = (_route, segments) => {
  const auth = inject(AuthService);
  if (auth.isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], {
    queryParams: { returnUrl: toUrl(segments) },
  });
};

/** Only the given roles; a logged-in user with another role sees "access denied". */
export function roleGuard(...roles: Role[]): CanMatchFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.isAuthenticated()) {
      return router.createUrlTree(['/login']);
    }
    return auth.hasRole(...roles) ? true : router.createUrlTree(['/acesso-negado']);
  };
}

/** Login and sign-up pages: a user who is already logged in goes to their home page. */
export const guestGuard: CanMatchFn = () => {
  const auth = inject(AuthService);
  return auth.isAuthenticated() ? inject(Router).parseUrl(auth.homeUrl()) : true;
};
