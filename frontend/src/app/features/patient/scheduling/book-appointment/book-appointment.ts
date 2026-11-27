import { BreakpointObserver } from '@angular/cdk/layout';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse, httpResource } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatStepper, MatStepperModule } from '@angular/material/stepper';
import { Router } from '@angular/router';
import {
  catchError,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  of,
  startWith,
  switchMap,
} from 'rxjs';
import { Specialty } from '../../../../core/api/catalog.models';
import { Availability, DayOfWeek, Doctor } from '../../../../core/api/doctor.models';
import { apiErrorMessage } from '../../../../core/http/api-error';
import { Page } from '../../../../core/http/page';
import { NotificationService } from '../../../../core/ui/notification.service';
import { startOfDayPlus, toIsoDate } from '../../../../shared/dates';
import { SchedulingApi } from '../scheduling.api';
import { FreeSlots } from '../scheduling.models';

/** Same horizon as the backend (medagenda.scheduling.max-days-ahead). */
const MAX_DAYS_AHEAD = 90;

/** JS Date.getDay(): 0 = Sunday ... 6 = Saturday. */
const JS_DAY: Record<DayOfWeek, number> = {
  SUNDAY: 0,
  MONDAY: 1,
  TUESDAY: 2,
  WEDNESDAY: 3,
  THURSDAY: 4,
  FRIDAY: 5,
  SATURDAY: 6,
};

interface DoctorSearchState {
  loading: boolean;
  doctors: Doctor[];
  failed?: boolean;
}

const SLOT_STEP_INDEX = 3;

/**
 * Booking wizard. Each choice lives in a signal; whatever depends on it (doctor list,
 * calendar filter, free slots) is derived with computed()/httpResource, so changing an
 * earlier step automatically refreshes the later ones.
 */
@Component({
  selector: 'app-book-appointment',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatStepperModule,
  ],
  templateUrl: './book-appointment.html',
  styleUrl: './book-appointment.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  // Date adapter only where the calendar is used: it stays in this lazy chunk, not in the initial bundle
  providers: [provideNativeDateAdapter(), { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' }],
})
export class BookAppointment {
  private readonly api = inject(SchedulingApi);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);
  private readonly stepper = viewChild.required(MatStepper);

  /** Vertical stepper on narrow screens: horizontal labels don't fit on a phone. */
  protected readonly orientation = toSignal(
    inject(BreakpointObserver)
      .observe('(max-width: 719px)')
      .pipe(map((state) => (state.matches ? 'vertical' : 'horizontal'))),
    { initialValue: 'horizontal' as const },
  );

  // ---- Step 1: specialty --------------------------------------------------------------
  protected readonly specialties = httpResource<Page<Specialty>>(
    () => '/api/specialties?size=100&sort=name',
  );
  protected readonly selectedSpecialty = signal<Specialty | null>(null);

  // ---- Step 2: doctor (search with debounceTime + switchMap) --------------------------
  protected readonly doctorSearch = new FormControl('', { nonNullable: true });
  protected readonly selectedDoctor = signal<Doctor | null>(null);

  /**
   * - debounceTime(300): waits for the user to stop typing instead of one request per key.
   * - distinctUntilChanged: no new request if the text did not actually change.
   * - switchMap: a new search cancels the previous in-flight request, so a slow old
   *   response can never overwrite the results of a newer search.
   * - combineLatest: a new specialty also triggers a new search.
   */
  protected readonly doctorResults = toSignal(
    combineLatest([
      toObservable(this.selectedSpecialty),
      this.doctorSearch.valueChanges.pipe(
        debounceTime(300),
        startWith(''),
        map((text) => text.trim()),
        distinctUntilChanged(),
      ),
    ]).pipe(
      switchMap(([specialty, name]) =>
        specialty
          ? this.api.searchDoctors(specialty.id, name).pipe(
              map((page): DoctorSearchState => ({ loading: false, doctors: page.content })),
              startWith<DoctorSearchState>({ loading: true, doctors: [] }),
              catchError(() => of<DoctorSearchState>({ loading: false, doctors: [], failed: true })),
            )
          : of<DoctorSearchState>({ loading: false, doctors: [] }),
      ),
    ),
    { initialValue: { loading: false, doctors: [] } },
  );

  // ---- Step 3: date -------------------------------------------------------------------
  protected readonly selectedDate = signal<Date | null>(null);
  protected readonly minDate = startOfDayPlus(0);
  protected readonly maxDate = startOfDayPlus(MAX_DAYS_AHEAD);

  private readonly availabilities = httpResource<Availability[]>(
    () => {
      const doctor = this.selectedDoctor();
      return doctor ? `/api/doctors/${doctor.id}/availabilities` : undefined;
    },
    { defaultValue: [] },
  );

  /** A new function whenever the doctor's working days change, so the calendar re-renders. */
  protected readonly dateFilter = computed(() => {
    const workingDays = new Set(this.availabilities.value().map((a) => JS_DAY[a.dayOfWeek]));
    return (date: Date | null) => !!date && workingDays.has(date.getDay());
  });
  protected readonly loadingDays = computed(() => this.availabilities.isLoading());

  // ---- Step 4: time -------------------------------------------------------------------
  protected readonly freeSlots = httpResource<FreeSlots>(() => {
    const doctor = this.selectedDoctor();
    const date = this.selectedDate();
    return doctor && date ? `/api/doctors/${doctor.id}/free-slots?date=${toIsoDate(date)}` : undefined;
  });
  protected readonly selectedTime = signal<string | null>(null);

  // ---- Step 5: confirmation -----------------------------------------------------------
  protected readonly submitting = signal(false);
  protected readonly bookingError = signal<string | null>(null);

  /** The date and time combined, for the summary ("2026-12-07T09:00:00" parsed as local). */
  protected readonly selectedDateTime = computed(() => {
    const date = this.selectedDate();
    const time = this.selectedTime();
    return date && time ? new Date(`${toIsoDate(date)}T${time}`) : null;
  });

  /*
   * Each choice clears the choices that depend on it. Done in the handlers (explicit and
   * synchronous) rather than with effect(), which Angular recommends against for
   * propagating state between signals.
   */
  protected selectSpecialty(specialty: Specialty): void {
    if (specialty.id === this.selectedSpecialty()?.id) {
      return;
    }
    this.selectedSpecialty.set(specialty);
    this.doctorSearch.setValue('');
    this.selectDoctor(null);
  }

  protected selectDoctor(doctor: Doctor | null): void {
    this.selectedDoctor.set(doctor);
    this.selectDate(null);
  }

  protected selectDate(date: Date | null): void {
    this.selectedDate.set(date);
    this.selectedTime.set(null);
    this.bookingError.set(null);
  }

  protected selectTime(time: string | null): void {
    this.selectedTime.set(time);
    this.bookingError.set(null);
  }

  protected confirm(): void {
    const doctor = this.selectedDoctor();
    const date = this.selectedDate();
    const time = this.selectedTime();
    if (!doctor || !date || !time) {
      return;
    }
    this.submitting.set(true);
    this.bookingError.set(null);

    this.api
      .book({ doctorId: doctor.id, scheduledAt: `${toIsoDate(date)}T${time}` })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Consulta agendada com sucesso!');
          void this.router.navigate(['/paciente/consultas']);
        },
        error: (error: unknown) => {
          this.bookingError.set(apiErrorMessage(error));
          // 409: someone took the slot meanwhile -> refresh the slots and go back to that step
          if (error instanceof HttpErrorResponse && error.status === 409) {
            this.selectedTime.set(null);
            this.freeSlots.reload();
            this.stepper().selectedIndex = SLOT_STEP_INDEX;
          }
        },
      });
  }

  protected formatTime(time: string): string {
    return time.slice(0, 5);
  }
}
