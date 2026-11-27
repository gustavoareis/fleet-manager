import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Observable } from 'rxjs';

export const NOTES_MAX_LENGTH = 2000;

export interface NotesDialogData {
  title: string;
  /** Who the dialog is about, shown under the title. */
  subtitle: string;
  confirmLabel: string;
  notes: string | null;
}

/**
 * Textarea dialog used for "complete", "no-show" and "edit notes". The result is the typed text
 * (possibly empty, which clears the notes), or undefined when the user cancels.
 */
@Component({
  selector: 'app-notes-dialog',
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <p class="subtitle">{{ data.subtitle }}</p>
      <mat-form-field class="field">
        <mat-label>Anotações clínicas</mat-label>
        <textarea
          matInput
          [formControl]="notes"
          rows="6"
          [attr.maxlength]="maxLength"
          cdkFocusInitial
        ></textarea>
        <mat-hint align="end">{{ notes.value.length }} / {{ maxLength }}</mat-hint>
      </mat-form-field>
      <p class="privacy">Somente você vê estas anotações. Elas não aparecem para o paciente nem para a recepção.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="undefined">Cancelar</button>
      <button mat-flat-button [mat-dialog-close]="notes.value">{{ data.confirmLabel }}</button>
    </mat-dialog-actions>
  `,
  styles: `
    .field {
      width: 100%;
    }
    .subtitle {
      margin: 0 0 12px;
      color: var(--mat-sys-on-surface-variant);
    }
    .privacy {
      font: var(--mat-sys-body-small);
      color: var(--mat-sys-on-surface-variant);
      margin: 4px 0 0;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotesDialog {
  protected readonly data = inject<NotesDialogData>(MAT_DIALOG_DATA);
  protected readonly maxLength = NOTES_MAX_LENGTH;
  protected readonly notes = new FormControl(this.data.notes ?? '', { nonNullable: true });
}

/** Opens the dialog; emits the text on confirm and nothing at all when the user cancels. */
export function askNotes(dialog: MatDialog, data: NotesDialogData): Observable<string | undefined> {
  return dialog
    .open<NotesDialog, NotesDialogData, string | undefined>(NotesDialog, { data, width: '520px' })
    .afterClosed();
}
