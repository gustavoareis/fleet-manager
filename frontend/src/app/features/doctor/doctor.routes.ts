import { Routes } from '@angular/router';

/** Lazy-loaded only for DOCTOR users (see roleGuard in app.routes.ts). */
export const DOCTOR_ROUTES: Routes = [
  {
    path: '',
    title: 'Agenda · MedAgenda',
    loadComponent: () => import('./agenda/doctor-agenda').then((m) => m.DoctorAgenda),
  },
  {
    path: 'horarios',
    title: 'Meus horários · MedAgenda',
    loadComponent: () => import('./doctor-home/doctor-home').then((m) => m.DoctorHome),
  },
];
