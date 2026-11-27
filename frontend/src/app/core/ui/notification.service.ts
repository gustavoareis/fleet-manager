import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Thin wrapper over MatSnackBar, so the rest of the app doesn't depend on Material directly.
 * The snack bar is announced by screen readers through an aria-live region.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.snackBar.open(message, 'Fechar', { duration: 4000, politeness: 'polite' });
  }

  error(message: string): void {
    this.snackBar.open(message, 'Fechar', { duration: 6000, politeness: 'assertive' });
  }
}
