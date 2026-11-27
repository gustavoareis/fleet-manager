import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { Page } from '../../../core/http/page';
import { CpfPipe, PhonePipe } from '../../../shared/pipes/br-format.pipes';
import { Patient } from '../patient.models';
import { Appointment } from '../scheduling/scheduling.models';

@Component({
  selector: 'app-patient-home',
  imports: [
    DatePipe,
    RouterLink,
    CpfPipe,
    PhonePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './patient-home.html',
  styleUrl: './patient-home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientHome {
  protected readonly auth = inject(AuthService);

  /** Re-requests automatically if the logged-in patient changes (the URL is a reactive function). */
  protected readonly patient = httpResource<Patient>(() => {
    const patientId = this.auth.user()?.patientId;
    return patientId ? `/api/patients/${patientId}` : undefined;
  });

  /** Upcoming list sorted by date: its first item is the next appointment. */
  private readonly upcoming = httpResource<Page<Appointment>>(
    () => '/api/appointments/me?scope=UPCOMING&size=1',
  );
  protected readonly nextAppointment = computed(() => this.upcoming.value()?.content[0] ?? null);
  protected readonly upcomingCount = computed(() => this.upcoming.value()?.totalElements ?? 0);
  protected readonly loadingNext = computed(() => this.upcoming.isLoading());

  protected asDate(value: string): Date {
    return new Date(value);
  }
}
