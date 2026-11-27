import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup } from '@angular/forms';
import { apiErrorMessage, applyServerErrors } from './api-error';

const problem = (status: number, extra: object = {}) =>
  new HttpErrorResponse({ status, error: { status, title: 'Erro', ...extra } });

describe('api-error', () => {
  describe('apiErrorMessage', () => {
    it('uses the detail sent by the backend', () => {
      expect(apiErrorMessage(problem(422, { detail: 'Horário indisponível.' }))).toBe(
        'Horário indisponível.',
      );
    });

    it('explains a network failure (status 0)', () => {
      expect(apiErrorMessage(new HttpErrorResponse({ status: 0 }))).toContain('conectar');
    });

    it('falls back to a generic message for unknown shapes', () => {
      expect(apiErrorMessage(new HttpErrorResponse({ status: 500, error: 'boom' }))).toContain(
        'inesperado',
      );
      expect(apiErrorMessage(new Error('x'))).toContain('inesperado');
    });
  });

  describe('applyServerErrors', () => {
    it('puts each field error on its control', () => {
      const form = new FormGroup({ name: new FormControl(''), cpf: new FormControl('') });
      const error = problem(400, { errors: [{ field: 'cpf', message: 'CPF inválido' }] });

      expect(applyServerErrors(form, error)).toBe(true);
      expect(form.get('cpf')?.errors).toEqual({ server: 'CPF inválido' });
      expect(form.get('cpf')?.touched).toBe(true);
      expect(form.get('name')?.errors).toBeNull();
    });

    it('ignores fields that are not in the form and non-problem errors', () => {
      const form = new FormGroup({ name: new FormControl('') });

      expect(applyServerErrors(form, problem(400, { errors: [{ field: 'x', message: 'm' }] }))).toBe(
        false,
      );
      expect(applyServerErrors(form, new Error('x'))).toBe(false);
    });
  });
});
