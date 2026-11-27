import { HealthInsurance } from '../../core/api/catalog.models';

export interface Patient {
  id: number;
  name: string;
  cpf: string;
  phone: string;
  birthDate: string;
  healthInsurance: HealthInsurance | null;
  email: string;
}
