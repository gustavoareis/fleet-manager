import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { todayIsoDate } from '../dates';

/** Removes everything that is not a digit ("123.456.789-09" -> "12345678909"). */
export function onlyDigits(value: string): string {
  return value.replace(/\D/g, '');
}

/**
 * Brazilian CPF check digits, the same rule the backend applies with @CPF.
 * Validating on the client gives instant feedback; the server still validates again.
 */
export function isValidCpf(value: string): boolean {
  const cpf = onlyDigits(value);
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return false; // wrong size, or "111.111.111-11"-style numbers that pass the math
  }
  const checkDigit = (length: number): number => {
    let sum = 0;
    for (let i = 0; i < length; i++) {
      sum += Number(cpf[i]) * (length + 1 - i);
    }
    const rest = (sum * 10) % 11;
    return rest === 10 ? 0 : rest;
  };
  return checkDigit(9) === Number(cpf[9]) && checkDigit(10) === Number(cpf[10]);
}

export const cpfValidator: ValidatorFn = (control: AbstractControl<string>) =>
  !control.value || isValidCpf(control.value) ? null : { cpf: true };

/** Area code + number: 10 digits (landline) or 11 (mobile). */
export const phoneValidator: ValidatorFn = (control: AbstractControl<string>) => {
  if (!control.value) {
    return null;
  }
  const length = onlyDigits(control.value).length;
  return length === 10 || length === 11 ? null : { phone: true };
};

/** Cross-field rule on the group: e.g. "password" and "confirmPassword" must be equal. */
export function matchingFields(field: string, confirmation: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const value = group.get(field)?.value;
    const confirmationValue = group.get(confirmation)?.value;
    return confirmationValue && value !== confirmationValue ? { fieldsMismatch: true } : null;
  };
}

/** The date must be strictly before today (birth dates). Works with "yyyy-mm-dd" strings. */
export const pastDateValidator: ValidatorFn = (control: AbstractControl<string>) => {
  if (!control.value) {
    return null;
  }
  return control.value < todayIsoDate() ? null : { pastDate: true };
};
