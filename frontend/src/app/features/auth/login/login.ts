import { ThemeToggle } from '../../../shared/ui/theme-toggle';
import { AuthHero } from '../../../shared/ui/auth-hero';
import { ChangeDetectionStrategy, Component, inject, input, isDevMode, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { apiErrorMessage } from '../../../core/http/api-error';

const DEMO_PASSWORD = 'Senha@123';

/** Only internal paths are accepted, so a crafted link can't redirect to another site after login. */
function safeReturnUrl(url: string | undefined): string | null {
  return url && url.startsWith('/') && !url.startsWith('//') ? url : null;
}

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    AuthHero,
    ThemeToggle,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Query params, bound by withComponentInputBinding(): /login?reason=expired&returnUrl=... */
  readonly reason = input<string>();
  readonly returnUrl = input<string>();

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly hidePassword = signal(true);

  /** Seeded test accounts exist only in the backend "dev" profile, so they are listed only in dev builds. */
  protected readonly demoAccounts = isDevMode()
    ? [
        { label: 'Paciente', email: 'paciente@medagenda.local' },
        { label: 'Médico', email: 'medico@medagenda.local' },
        { label: 'Recepção', email: 'recepcao@medagenda.local' },
        { label: 'Admin', email: 'admin@medagenda.local' },
      ]
    : [];
  protected readonly demoPassword = DEMO_PASSWORD;

  // NonNullableFormBuilder: form.value is typed as { email: string; password: string }
  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage.set(null);
    this.submitting.set(true);

    this.auth
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (user) => {
          void this.router.navigateByUrl(
            safeReturnUrl(this.returnUrl()) ?? this.auth.homeUrl(user.role),
          );
        },
        error: (error: unknown) => this.errorMessage.set(apiErrorMessage(error)),
      });
  }

  protected useDemoAccount(email: string): void {
    this.form.setValue({ email, password: DEMO_PASSWORD });
  }
}
