import { Specialty } from './catalog.models';

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: 'Segunda-feira',
  TUESDAY: 'Terça-feira',
  WEDNESDAY: 'Quarta-feira',
  THURSDAY: 'Quinta-feira',
  FRIDAY: 'Sexta-feira',
  SATURDAY: 'Sábado',
  SUNDAY: 'Domingo',
};

export interface Doctor {
  id: number;
  name: string;
  crm: string;
  specialty: Specialty;
  email: string;
}

export interface Availability {
  id: number;
  dayOfWeek: DayOfWeek;
  /** "HH:mm:ss" */
  startTime: string;
  endTime: string;
  slotDurationMinutes: number;
}
