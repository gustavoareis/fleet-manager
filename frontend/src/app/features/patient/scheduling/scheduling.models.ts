import { AppointmentStatus } from '../../../core/api/appointment-status';

/** Mirrors com.medagenda.scheduling DTOs. */

export type AppointmentScope = 'UPCOMING' | 'HISTORY';

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
