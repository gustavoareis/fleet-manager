import { HttpErrorResponse } from '@angular/common/http';
import { FormGroup } from '@angular/forms';

/** RFC 9457 body produced by the backend's GlobalExceptionHandler. */
export interface ProblemDetail {
  status: number;
  title: string;
  detail?: string;
  errors?: { field: string; message: string }[];
}

function isProblemDetail(body: unknown): body is ProblemDetail {
  return typeof body === 'object' && body !== null && 'status' in body && 'title' in body;
}

/** Turns any HTTP failure into a sentence that can be shown to the user. */
export function apiErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
    }
    if (isProblemDetail(error.error) && error.error.detail) {
      return error.error.detail;
    }
  }
  return 'Ocorreu um erro inesperado. Tente novamente.';
}

/**
 * Copies field errors from a 400 response onto the matching form controls, so the message
 * appears under the right input. Returns true when at least one control received an error.
 */
export function applyServerErrors(form: FormGroup, error: unknown): boolean {
  if (!(error instanceof HttpErrorResponse) || !isProblemDetail(error.error)) {
    return false;
  }
  let applied = false;
  for (const { field, message } of error.error.errors ?? []) {
    const control = form.get(field);
    if (control) {
      control.setErrors({ ...control.errors, server: message });
      control.markAsTouched();
      applied = true;
    }
  }
  return applied;
}
