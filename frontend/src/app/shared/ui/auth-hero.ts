import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Brand panel of the public pages. Decorative only (aria-hidden): a heartbeat trace that sweeps
 * along a flat line.
 */
@Component({
  selector: 'app-auth-hero',
  imports: [MatIconModule],
  template: `
    <aside class="hero" aria-hidden="true">
      <div class="brand">
        <span class="logo"><mat-icon>ecg_heart</mat-icon></span>
        <span>MedAgenda</span>
      </div>

      <p class="headline">Sua consulta,<br />no horário certo.</p>

      <svg class="trace" viewBox="0 0 600 120" preserveAspectRatio="none" fill="none">
        <path class="ghost" [attr.d]="path" />
        <path class="line" [attr.d]="path" pathLength="1" />
      </svg>

    </aside>
  `,
  styles: `
    :host {
      display: none;
    }

    @media (min-width: 960px) {
      :host {
        display: block;
      }
    }

    .hero {
      position: relative;
      display: flex;
      flex-direction: column;
      height: 100%;
      min-height: calc(100vh - 32px);
      box-sizing: border-box;
      padding: 40px;
      overflow: hidden;
      border-radius: 32px;
      color: #eafff5;
      background:
        radial-gradient(520px 360px at 100% 100%, rgb(123 224 176 / 28%), transparent 70%),
        linear-gradient(155deg, #0b6e79 0%, #07424a 60%, #052b31 100%);
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-family: var(--ma-display);
      font-size: 1.35rem;
      font-weight: 500;
    }

    .logo {
      display: grid;
      place-items: center;
      width: 38px;
      height: 38px;
      border-radius: 12px;
      background: #7be0b0;
      color: #05452d;
    }

    .headline {
      margin: 12vh 0 0;
      font-family: var(--ma-display);
      font-size: clamp(2.6rem, 1.6rem + 2.6vw, 4rem);
      font-weight: 300;
      line-height: 1.05;
      letter-spacing: -0.03em;
      animation: rise 900ms 100ms var(--ma-ease) both;
    }

    .trace {
      width: calc(100% + 80px);
      height: 120px;
      margin: 40px -40px 0;
    }

    .ghost {
      stroke: rgb(255 255 255 / 12%);
      stroke-width: 2;
    }

    .line {
      stroke: #7be0b0;
      stroke-width: 2.5;
      stroke-linecap: round;
      stroke-linejoin: round;
      stroke-dasharray: 0.34 1;
      stroke-dashoffset: 0.34;
      filter: drop-shadow(0 0 6px rgb(123 224 176 / 70%));
      animation: sweep 3.6s 400ms cubic-bezier(0.45, 0, 0.3, 1) infinite;
    }

    @keyframes rise {
      from {
        opacity: 0;
        transform: translateY(18px);
      }
    }

    /* A bright segment travels along the trace; the dashed gap hides the rest */
    @keyframes sweep {
      from {
        stroke-dashoffset: 0.34;
      }
      to {
        stroke-dashoffset: -1;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthHero {
  /** Flat line with two QRS complexes (the heartbeat spikes). */
  protected readonly path =
    'M0 70 H110 l12 -10 l12 10 H190 l10 14 l16 -78 l16 100 l12 -36 H330 l12 -12 l14 12 H430 l10 14 l16 -70 l16 92 l12 -30 H600';
}
