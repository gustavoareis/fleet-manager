import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AuthService } from '../../../core/auth/auth.service';
import { Availability, DAY_LABELS, Doctor } from '../../../core/api/doctor.models';

function minutes(time: string): number {
  const [hours, mins] = time.split(':').map(Number);
  return hours * 60 + mins;
}

/** Doctor landing page. The daily/weekly agenda is added in the doctor-area step. */
@Component({
  selector: 'app-doctor-home',
  imports: [MatCardModule, MatIconModule, MatProgressBarModule],
  templateUrl: './doctor-home.html',
  styleUrl: './doctor-home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DoctorHome {
  protected readonly auth = inject(AuthService);
  protected readonly dayLabels = DAY_LABELS;

  private readonly doctorId = computed(() => this.auth.user()?.doctorId);

  protected readonly doctor = httpResource<Doctor>(() =>
    this.doctorId() ? `/api/doctors/${this.doctorId()}` : undefined,
  );

  protected readonly availabilities = httpResource<Availability[]>(
    () => (this.doctorId() ? `/api/doctors/${this.doctorId()}/availabilities` : undefined),
    { defaultValue: [] },
  );

  /** Derived values: recalculated only when the availabilities change. */
  protected readonly weeklySlots = computed(() =>
    this.availabilities
      .value()
      .reduce(
        (total, a) =>
          total + Math.floor((minutes(a.endTime) - minutes(a.startTime)) / a.slotDurationMinutes),
        0,
      ),
  );

  protected readonly weeklyHours = computed(
    () =>
      this.availabilities
        .value()
        .reduce((total, a) => total + minutes(a.endTime) - minutes(a.startTime), 0) / 60,
  );

  protected time(value: string): string {
    return value.slice(0, 5);
  }
}
