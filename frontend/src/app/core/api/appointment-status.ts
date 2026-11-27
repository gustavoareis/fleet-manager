export type AppointmentStatus = 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';

export const STATUS_LABELS: Record<AppointmentStatus, string> = {
  SCHEDULED: 'Agendada',
  CONFIRMED: 'Confirmada',
  CANCELLED: 'Cancelada',
  COMPLETED: 'Realizada',
  NO_SHOW: 'Não compareceu',
};

/** States that still occupy the time slot (mirrors Appointment.ACTIVE_STATUSES in the backend). */
export function isActiveStatus(status: AppointmentStatus): boolean {
  return status === 'SCHEDULED' || status === 'CONFIRMED';
}
