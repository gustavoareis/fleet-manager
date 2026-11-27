import { Pipe, PipeTransform } from '@angular/core';

/** "12345678909" -> "123.456.789-09". Pure pipe: recalculated only when the input changes. */
@Pipe({ name: 'cpf' })
export class CpfPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    const digits = value?.replace(/\D/g, '') ?? '';
    return digits.length === 11
      ? digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
      : (value ?? '');
  }
}

/** "11987654321" -> "(11) 98765-4321"; "1133334444" -> "(11) 3333-4444". */
@Pipe({ name: 'phone' })
export class PhonePipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    const digits = value?.replace(/\D/g, '') ?? '';
    if (digits.length === 11) {
      return digits.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }
    if (digits.length === 10) {
      return digits.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    }
    return value ?? '';
  }
}
