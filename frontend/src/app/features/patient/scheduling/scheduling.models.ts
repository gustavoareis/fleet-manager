/** Mirrors com.medagenda.scheduling DTOs. */

export type AppointmentStatus = 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';

export type AppointmentScope = 'UPCOMING' | 'HISTORY';

export const STATUS_LABELS: Record<AppointmentStatus, string> = {
  SCHEDULED: 'Agendada',
  CONFIRMED: 'Confirmada',
  CANCELLED: 'Cancelada',
  COMPLETED: 'Realizada',
  NO_SHOW: 'Não compareceu',
};

export interface Appointment {
  id: number;
  /** Clinic-local "yyyy-MM-ddTHH:mm:ss". */
  scheduledAt: string;
  status: AppointmentStatus;
  doctor: { id: number; name: string; specialty: string };
  patient: { id: number; name: string };
  /** Computed by the server (active + 24h rule). */
  cancellable: boolean;
}

export interface FreeSlots {
  date: string;
  /** "HH:mm:ss" */
  slots: string[];
}

export interface BookingRequest {
  doctorId: number;
  scheduledAt: string;
}
