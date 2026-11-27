import { AbstractControl, FormGroupDirective, NgForm } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';

/**
 * By default mat-form-field only turns red when its own control is invalid. For a rule that
 * lives on the parent group (e.g. "passwords must match"), this matcher also shows the field
 * as invalid when the group has the given error.
 */
export class CrossFieldErrorMatcher implements ErrorStateMatcher {
  constructor(private readonly groupError: string) {}

  isErrorState(control: AbstractControl | null, form: FormGroupDirective | NgForm | null): boolean {
    const touched = !!control && (control.touched || !!form?.submitted);
    const invalid = !!control?.invalid || !!form?.form.hasError(this.groupError);
    return touched && invalid;
  }
}
