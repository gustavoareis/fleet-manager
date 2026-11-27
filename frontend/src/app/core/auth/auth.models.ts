/** Mirrors the backend contracts in com.medagenda.auth. */

export type Role = 'PATIENT' | 'DOCTOR' | 'RECEPTIONIST' | 'ADMIN';

export const ROLE_LABELS: Record<Role, string> = {
  PATIENT: 'Paciente',
  DOCTOR: 'Médico(a)',
  RECEPTIONIST: 'Recepção',
  ADMIN: 'Administrador(a)',
};

export interface UserInfo {
  id: number;
  email: string;
  role: Role;
  displayName: string;
  patientId: number | null;
  doctorId: number | null;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  /** Seconds until the token expires. */
  expiresIn: number;
  user: UserInfo;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterPatientRequest {
  name: string;
  cpf: string;
  phone: string;
  birthDate: string;
  healthInsuranceId: number | null;
  email: string;
  password: string;
}

/** What the app keeps between page loads. */
export interface Session {
  token: string;
  /** Epoch milliseconds. */
  expiresAt: number;
  user: UserInfo;
}
