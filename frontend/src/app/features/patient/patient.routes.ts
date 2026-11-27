import { Routes } from '@angular/router';

/** Lazy-loaded only for PATIENT users (see roleGuard in app.routes.ts). */
export const PATIENT_ROUTES: Routes = [
  {
    path: '',
    title: 'Início · MedAgenda',
    loadComponent: () => import('./patient-home/patient-home').then((m) => m.PatientHome),
  },
  {
    path: 'agendar',
    title: 'Agendar consulta · MedAgenda',
    loadComponent: () =>
      import('./scheduling/book-appointment/book-appointment').then((m) => m.BookAppointment),
  },
  {
    path: 'consultas',
    title: 'Minhas consultas · MedAgenda',
    loadComponent: () =>
      import('./scheduling/my-appointments/my-appointments').then((m) => m.MyAppointments),
  },
];
