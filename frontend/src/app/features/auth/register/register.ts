import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { HealthInsurance } from '../../../core/api/catalog.models';
import { AuthService } from '../../../core/auth/auth.service';
import { apiErrorMessage, applyServerErrors } from '../../../core/http/api-error';
import { Page } from '../../../core/http/page';
import { NotificationService } from '../../../core/ui/notification.service';
import { CrossFieldErrorMatcher } from '../../../shared/forms/cross-field-error-matcher';
import {
  cpfValidator,
  matchingFields,
  onlyDigits,
  pastDateValidator,
  phoneValidator,
  todayIsoDate,
} from '../../../shared/validators/br-validators';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(NonNullableFormBuilder);

  /**
   * httpResource: the request becomes a signal-based resource with value(), isLoading()
   * and error(), so the template needs no subscribe/unsubscribe.
   */
  protected readonly insurances = httpResource<Page<HealthInsurance>>(
    () => '/api/health-insurances?active=true&size=100&sort=name',
  );

  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly hidePassword = signal(true);
  protected readonly maxBirthDate = todayIsoDate();
  protected readonly passwordMatcher = new CrossFieldErrorMatcher('fieldsMismatch');

  protected readonly form = this.fb.group(
    {
      name: ['', [Validators.required, Validators.maxLength(120)]],
      cpf: ['', [Validators.required, cpfValidator]],
      phone: ['', [Validators.required, phoneValidator]],
      birthDate: ['', [Validators.required, pastDateValidator]],
      // Explicit generic: the value is a number, or null for "particular" (no insurance)
      healthInsuranceId: this.fb.control<number | null>(null),
      email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: matchingFields('password', 'confirmPassword') },
  );

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.errorMessage.set(null);
    this.submitting.set(true);

    this.auth
      .register({
        name: value.name.trim(),
        cpf: onlyDigits(value.cpf),
        phone: onlyDigits(value.phone),
        birthDate: value.birthDate,
        healthInsuranceId: value.healthInsuranceId,
        email: value.email.trim(),
        password: value.password,
      })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (user) => {
          this.notifications.success('Cadastro realizado. Bem-vindo(a) ao MedAgenda!');
          void this.router.navigateByUrl(this.auth.homeUrl(user.role));
        },
        error: (error: unknown) => {
          // 400 with field errors: show them under each input; otherwise a general message
          if (!applyServerErrors(this.form, error)) {
            this.errorMessage.set(apiErrorMessage(error));
          }
        },
      });
  }
}
