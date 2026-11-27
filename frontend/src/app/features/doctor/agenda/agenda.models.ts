import { AppointmentStatus } from '../../../core/api/appointment-status';

/** Mirrors com.medagenda.scheduling.DoctorAppointmentResponse. */
export interface DoctorAppointment {
  id: number;
  /** Clinic-local "yyyy-MM-ddTHH:mm:ss". */
  scheduledAt: string;
  status: AppointmentStatus;
  patient: {
    id: number;
    name: string;
    birthDate: string;
    phone: string;
    insurance: string | null;
  };
  /** Clinical notes; only the doctor receives them. */
  notes: string | null;
  /** Computed by the server: active status and the appointment time has arrived. */
  canRecordOutcome: boolean;
}

export type AgendaView = 'day' | 'week';

export interface AgendaDay {
  /** "yyyy-mm-dd" */
  iso: string;
  date: Date;
  isToday: boolean;
  appointments: DoctorAppointment[];
}
