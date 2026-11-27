import { DatePipe } from '@angular/common';
import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Observable, filter, switchMap } from 'rxjs';
import { STATUS_LABELS, isActiveStatus } from '../../../core/api/appointment-status';
import { AuthService } from '../../../core/auth/auth.service';
import { apiErrorMessage } from '../../../core/http/api-error';
import { NotificationService } from '../../../core/ui/notification.service';
import {
  addDays,
  ageFrom,
  parseIsoDate,
  startOfDayPlus,
  startOfWeek,
  toIsoDate,
} from '../../../shared/dates';
import { PhonePipe } from '../../../shared/pipes/br-format.pipes';
import { AgendaApi } from './agenda.api';
import { AgendaDay, AgendaView, DoctorAppointment } from './agenda.models';
import { askNotes } from './notes-dialog';

type Outcome = 'complete' | 'no-show';

const OUTCOME_COPY: Record<Outcome, { title: string; confirm: string; success: string }> = {
  complete: { title: 'Concluir consulta', confirm: 'Concluir', success: 'Consulta marcada como realizada.' },
  'no-show': { title: 'Registrar falta', confirm: 'Registrar falta', success: 'Falta registrada.' },
};

/**
 * Doctor's agenda. The view (day/week) and the reference date are signals; the date range, the
 * request, the per-day grouping and the counters are all derived from them, so changing either
 * signal refreshes everything without manual wiring.
 */
@Component({
  selector: 'app-doctor-agenda',
  imports: [
    DatePipe,
    PhonePipe,
    MatButtonModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
  ],
  templateUrl: './doctor-agenda.html',
  styleUrl: './doctor-agenda.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DoctorAgenda {
  private readonly auth = inject(AuthService);
  private readonly api = inject(AgendaApi);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);

  protected readonly statusLabels = STATUS_LABELS;
  protected readonly firstName = this.auth.firstName;

  protected readonly view = signal<AgendaView>('day');
  protected readonly reference = signal<Date>(startOfDayPlus(0));
  protected readonly busyId = signal<number | null>(null);

  /** Inclusive date range shown: one day, or Monday to Sunday of the reference week. */
  protected readonly range = computed(() => {
    const reference = this.reference();
    if (this.view() === 'day') {
      return { from: reference, to: reference };
    }
    const monday = startOfWeek(reference);
    return { from: monday, to: addDays(monday, 6) };
  });

  protected readonly appointments = httpResource<DoctorAppointment[]>(
    () => {
      const doctorId = this.auth.user()?.doctorId;
      if (!doctorId) {
        return undefined;
      }
      const { from, to } = this.range();
      return `/api/doctors/${doctorId}/appointments?from=${toIsoDate(from)}&to=${toIsoDate(to)}`;
    },
    { defaultValue: [] },
  );

  /** One entry per day of the range (also empty days, so the week view shows "no appointments"). */
  protected readonly days = computed<AgendaDay[]>(() => {
    const { from, to } = this.range();
    const todayIso = toIsoDate(new Date());
    // The list arrives sorted by time, so each day's group keeps that order
    const byDay = new Map<string, DoctorAppointment[]>();
    for (const appointment of this.appointments.value()) {
      const iso = appointment.scheduledAt.slice(0, 10);
      byDay.set(iso, [...(byDay.get(iso) ?? []), appointment]);
    }
    const days: AgendaDay[] = [];
    for (let date = from; date <= to; date = addDays(date, 1)) {
      const iso = toIsoDate(date);
      days.push({ iso, date, isToday: iso === todayIso, appointments: byDay.get(iso) ?? [] });
    }
    return days;
  });

  /** Counters for the range on screen. Cancelled appointments are counted apart, not in the total. */
  protected readonly stats = computed(() => {
    const list = this.appointments.value();
    const count = (predicate: (a: DoctorAppointment) => boolean) => list.filter(predicate).length;
    return {
      total: count((a) => a.status !== 'CANCELLED'),
      waiting: count((a) => isActiveStatus(a.status)),
      completed: count((a) => a.status === 'COMPLETED'),
      noShow: count((a) => a.status === 'NO_SHOW'),
      cancelled: count((a) => a.status === 'CANCELLED'),
    };
  });

  /**
   * True while a different day/week is being fetched. During that moment value() is the empty
   * default, so the counters show "–" instead of a misleading 0. (Reloading after an action keeps
   * the previous data, so counters don't flicker then.)
   */
  protected readonly changingPeriod = computed(() => this.appointments.status() === 'loading');

  protected readonly isCurrentPeriod = computed(() => {
    const { from, to } = this.range();
    const today = startOfDayPlus(0);
    return today >= from && today <= to;
  });

  protected readonly referenceIso = computed(() => toIsoDate(this.reference()));

  // ---- navigation ----------------------------------------------------------------------

  protected changeView(view: AgendaView): void {
    this.view.set(view);
  }

  /** Moves a day (day view) or a week (week view) backwards or forwards. */
  protected step(direction: -1 | 1): void {
    this.reference.update((date) => addDays(date, direction * (this.view() === 'day' ? 1 : 7)));
  }

  protected goToToday(): void {
    this.reference.set(startOfDayPlus(0));
  }

  protected goToDate(iso: string): void {
    if (iso) {
      this.reference.set(parseIsoDate(iso));
    }
  }

  // ---- actions -------------------------------------------------------------------------

  protected record(appointment: DoctorAppointment, outcome: Outcome): void {
    const copy = OUTCOME_COPY[outcome];
    this.run(
      askNotes(this.dialog, {
        title: copy.title,
        subtitle: `${appointment.patient.name} · ${this.time(appointment)}`,
        confirmLabel: copy.confirm,
        notes: appointment.notes,
      }),
      appointment,
      (notes) =>
        outcome === 'complete'
          ? this.api.complete(appointment.id, notes)
          : this.api.markNoShow(appointment.id, notes),
      copy.success,
    );
  }

  protected editNotes(appointment: DoctorAppointment): void {
    this.run(
      askNotes(this.dialog, {
        title: 'Anotações da consulta',
        subtitle: `${appointment.patient.name} · ${this.time(appointment)}`,
        confirmLabel: 'Salvar',
        notes: appointment.notes,
      }),
      appointment,
      (notes) => this.api.saveNotes(appointment.id, notes),
      'Anotações salvas.',
    );
  }

  /** Dialog result (text, or nothing if cancelled) -> API call -> feedback -> reload the agenda. */
  private run(
    dialogResult: Observable<string | undefined>,
    appointment: DoctorAppointment,
    call: (notes: string) => Observable<DoctorAppointment>,
    successMessage: string,
  ): void {
    dialogResult
      .pipe(
        filter((notes): notes is string => notes !== undefined),
        switchMap((notes) => {
          this.busyId.set(appointment.id);
          return call(notes);
        }),
      )
      .subscribe({
        next: () => {
          this.busyId.set(null);
          this.notifications.success(successMessage);
          this.appointments.reload();
        },
        error: (error: unknown) => {
          this.busyId.set(null);
          this.notifications.error(apiErrorMessage(error));
          this.appointments.reload();
        },
      });
  }

  // ---- presentation helpers ------------------------------------------------------------

  protected age(appointment: DoctorAppointment): number {
    return ageFrom(appointment.patient.birthDate);
  }

  protected isActive(appointment: DoctorAppointment): boolean {
    return isActiveStatus(appointment.status);
  }

  protected time(appointment: DoctorAppointment): string {
    return appointment.scheduledAt.slice(11, 16);
  }
}
