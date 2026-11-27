import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ROLE_LABELS, Role } from '../auth/auth.models';
import { AuthService } from '../auth/auth.service';

interface NavItem {
  label: string;
  icon: string;
  path: string;
}

/** Links each role sees. Later steps add entries here (booking, my appointments, agenda...). */
const NAV_BY_ROLE: Record<Role, NavItem[]> = {
  PATIENT: [{ label: 'Início', icon: 'home', path: '/paciente' }],
  DOCTOR: [{ label: 'Agenda', icon: 'calendar_month', path: '/medico' }],
  RECEPTIONIST: [{ label: 'Início', icon: 'home', path: '/equipe' }],
  ADMIN: [{ label: 'Início', icon: 'home', path: '/equipe' }],
};

/** Frame of every authenticated page: toolbar + navigation + the routed page. */
@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatToolbarModule,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Shell {
  protected readonly auth = inject(AuthService);

  protected readonly navItems = computed(() => {
    const role = this.auth.role();
    return role ? NAV_BY_ROLE[role] : [];
  });

  protected readonly roleLabel = computed(() => {
    const role = this.auth.role();
    return role ? ROLE_LABELS[role] : '';
  });

  protected logout(): void {
    this.auth.logout();
  }
}
