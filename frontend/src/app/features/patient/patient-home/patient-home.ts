import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../../core/auth/auth.service';
import { CpfPipe, PhonePipe } from '../../../shared/pipes/br-format.pipes';
import { Patient } from '../patient.models';

/** Patient landing page. Booking and "my appointments" are added in the scheduling step. */
@Component({
  selector: 'app-patient-home',
  imports: [DatePipe, CpfPipe, PhonePipe, MatCardModule, MatIconModule, MatProgressBarModule],
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
}
