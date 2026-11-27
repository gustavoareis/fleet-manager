import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth/auth.guards';
import { AuthService } from './core/auth/auth.service';

/**
 * Every page is lazy-loaded (loadComponent / loadChildren): the initial bundle holds only
 * the framework and the core, and each feature is downloaded the first time it is opened.
 */
export const routes: Routes = [
  {
    path: 'login',
    title: 'Entrar · MedAgenda',
    canMatch: [guestGuard],
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'cadastro',
    title: 'Criar conta · MedAgenda',
    canMatch: [guestGuard],
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
  },
  {
    // Authenticated area: everything below renders inside the shell (toolbar + navigation)
    path: '',
    canMatch: [authGuard],
    loadComponent: () => import('./core/layout/shell').then((m) => m.Shell),
    children: [
      {
        path: 'paciente',
        canMatch: [roleGuard('PATIENT')],
        loadChildren: () => import('./features/patient/patient.routes').then((m) => m.PATIENT_ROUTES),
      },
      {
        path: 'medico',
        canMatch: [roleGuard('DOCTOR')],
        loadChildren: () => import('./features/doctor/doctor.routes').then((m) => m.DOCTOR_ROUTES),
      },
      {
        path: 'equipe',
        title: 'Início · MedAgenda',
        canMatch: [roleGuard('RECEPTIONIST', 'ADMIN')],
        loadComponent: () => import('./features/staff/staff-home').then((m) => m.StaffHome),
      },
      {
        path: 'acesso-negado',
        title: 'Acesso negado · MedAgenda',
        loadComponent: () => import('./shared/pages/status-page').then((m) => m.StatusPage),
        data: {
          icon: 'block',
          heading: 'Acesso negado',
          message: 'Seu perfil não tem permissão para acessar esta página.',
        },
      },
      {
        // "/" sends each user to the home page of their role
        path: '',
        pathMatch: 'full',
        redirectTo: () => inject(AuthService).homeUrl(),
      },
      {
        path: '**',
        title: 'Página não encontrada · MedAgenda',
        loadComponent: () => import('./shared/pages/status-page').then((m) => m.StatusPage),
        data: {
          icon: 'search_off',
          heading: 'Página não encontrada',
          message: 'O endereço acessado não existe.',
        },
      },
    ],
  },
];
