import { Routes } from '@angular/router';

/** Lazy-loaded only for PATIENT users (see roleGuard in app.routes.ts). */
export const PATIENT_ROUTES: Routes = [
  {
    path: '',
    title: 'Início · MedAgenda',
    loadComponent: () => import('./patient-home/patient-home').then((m) => m.PatientHome),
  },
];
