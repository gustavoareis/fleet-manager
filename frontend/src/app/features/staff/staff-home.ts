import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth/auth.service';

/**
 * Reception and admin work in the legacy JSP module served by the backend.
 * This page only points them there.
 */
@Component({
  selector: 'app-staff-home',
  imports: [MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <section class="page">
      <h1>Olá!</h1>
      <p class="page-subtitle">Você entrou como {{ auth.user()?.email }}.</p>
      <mat-card appearance="outlined">
        <mat-card-header>
          <mat-icon mat-card-avatar aria-hidden="true">admin_panel_settings</mat-icon>
          <h2 matCardTitle>Módulo administrativo</h2>
        </mat-card-header>
        <mat-card-content>
          <p>
            Cadastros de convênios e especialidades, consultas do dia e relatórios ficam no
            módulo administrativo da clínica.
          </p>
        </mat-card-content>
        <mat-card-actions>
          <a mat-flat-button href="/admin">Abrir módulo administrativo</a>
        </mat-card-actions>
      </mat-card>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StaffHome {
  protected readonly auth = inject(AuthService);
}
