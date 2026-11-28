import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { ThemeService } from '../../core/ui/theme.service';

/** Round button that switches between light and dark mode. */
@Component({
  selector: 'app-theme-toggle',
  imports: [MatIconModule],
  template: `
    <button
      type="button"
      class="toggle"
      (click)="themes.toggle()"
      [attr.aria-label]="themes.theme() === 'dark' ? 'Ativar modo claro' : 'Ativar modo escuro'"
    >
      <mat-icon aria-hidden="true">{{ themes.theme() === 'dark' ? 'light_mode' : 'dark_mode' }}</mat-icon>
    </button>
  `,
  styles: `
    .toggle {
      display: grid;
      place-items: center;
      width: 44px;
      height: 44px;
      border: 0;
      border-radius: 50%;
      background: var(--ma-card);
      color: var(--ma-ink);
      box-shadow: var(--ma-shadow);
      cursor: pointer;
      transition:
        transform 300ms var(--ma-ease),
        background-color 200ms;
    }

    .toggle:hover {
      background: var(--ma-pulse-soft);
    }

    .toggle:hover mat-icon {
      transform: rotate(25deg);
    }

    .toggle:active {
      transform: scale(0.92);
    }

    mat-icon {
      transition: transform 300ms var(--ma-ease);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ThemeToggle {
  protected readonly themes = inject(ThemeService);
}
