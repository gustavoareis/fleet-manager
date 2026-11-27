import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterLink } from '@angular/router';
import { filter, switchMap } from 'rxjs';
import { STATUS_LABELS } from '../../../../core/api/appointment-status';
import { apiErrorMessage } from '../../../../core/http/api-error';
import { Page } from '../../../../core/http/page';
import { NotificationService } from '../../../../core/ui/notification.service';
import { confirm } from '../../../../shared/ui/confirm-dialog';
import { SchedulingApi } from '../scheduling.api';
import { Appointment, AppointmentScope } from '../scheduling.models';

const SCOPES: AppointmentScope[] = ['UPCOMING', 'HISTORY'];

@Component({
  selector: 'app-my-appointments',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTabsModule,
  ],
  templateUrl: './my-appointments.html',
  styleUrl: './my-appointments.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MyAppointments {
  private readonly api = inject(SchedulingApi);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);

  protected readonly statusLabels = STATUS_LABELS;
  protected readonly scope = signal<AppointmentScope>('UPCOMING');
  protected readonly pageIndex = signal(0);
  protected readonly pageSize = signal(10);
  protected readonly cancellingId = signal<number | null>(null);

  /** Any change to tab, page or page size builds a new URL, and httpResource refetches. */
  protected readonly appointments = httpResource<Page<Appointment>>(
    () =>
      `/api/appointments/me?scope=${this.scope()}&page=${this.pageIndex()}&size=${this.pageSize()}`,
  );

  protected changeTab(index: number): void {
    this.scope.set(SCOPES[index]);
    this.pageIndex.set(0);
  }

  protected changePage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  protected cancel(appointment: Appointment): void {
    confirm(this.dialog, {
      title: 'Cancelar consulta?',
      message: `A consulta com ${appointment.doctor.name} será cancelada e o horário ficará livre para outros pacientes.`,
      confirmLabel: 'Cancelar consulta',
    })
      .pipe(
        filter(Boolean),
        switchMap(() => {
          this.cancellingId.set(appointment.id);
          return this.api.cancel(appointment.id);
        }),
      )
      .subscribe({
        next: () => {
          this.cancellingId.set(null);
          this.notifications.success('Consulta cancelada.');
          this.appointments.reload();
        },
        error: (error: unknown) => {
          this.cancellingId.set(null);
          this.notifications.error(apiErrorMessage(error));
        },
      });
  }

  /** The API sends clinic-local times without offset; new Date() reads them as local time. */
  protected asDate(value: string): Date {
    return new Date(value);
  }
}
