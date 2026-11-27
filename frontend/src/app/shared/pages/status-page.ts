import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Generic full-page message (access denied, not found). The texts come from the route's
 * "data", bound to these inputs by withComponentInputBinding().
 */
@Component({
  selector: 'app-status-page',
  imports: [MatButtonModule, MatIconModule, RouterLink],
  template: `
    <section class="status" [attr.aria-labelledby]="'status-title'">
      <mat-icon class="icon" aria-hidden="true">{{ icon() }}</mat-icon>
      <h1 id="status-title">{{ heading() }}</h1>
      <p>{{ message() }}</p>
      <a mat-flat-button [routerLink]="auth.homeUrl()">Ir para o início</a>
    </section>
  `,
  styles: `
    .status {
      display: grid;
      justify-items: center;
      text-align: center;
      gap: 8px;
      padding: 64px 16px;
    }
    .icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: var(--mat-sys-primary);
    }
    h1 {
      font: var(--mat-sys-headline-medium);
      margin: 0;
    }
    p {
      color: var(--mat-sys-on-surface-variant);
      margin: 0 0 16px;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StatusPage {
  protected readonly auth = inject(AuthService);

  readonly icon = input('info');
  readonly heading = input('');
  readonly message = input('');
}
