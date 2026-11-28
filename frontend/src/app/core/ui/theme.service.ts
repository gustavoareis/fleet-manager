import { DOCUMENT } from '@angular/common';
import { Injectable, computed, inject, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

const STORAGE_KEY = 'medagenda.theme';

/**
 * Light/dark mode. Until the user picks one, the OS preference applies; after that the choice is
 * stored and forced through the "color-scheme" CSS property (the Material theme uses light-dark()).
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);

  private readonly chosen = signal<Theme | null>(this.readStored());

  /** Theme currently shown (the explicit choice, otherwise the OS preference). */
  readonly theme = computed<Theme>(() => this.chosen() ?? this.systemTheme());

  constructor() {
    this.apply();
  }

  toggle(): void {
    this.chosen.set(this.theme() === 'dark' ? 'light' : 'dark');
    try {
      localStorage.setItem(STORAGE_KEY, this.chosen()!);
    } catch {
      // Storage can be blocked (private mode): the choice then lasts only for this visit
    }
    this.apply();
  }

  private apply(): void {
    this.document.documentElement.style.colorScheme = this.chosen() ?? 'light dark';
  }

  private readStored(): Theme | null {
    try {
      const value = localStorage.getItem(STORAGE_KEY);
      return value === 'light' || value === 'dark' ? value : null;
    } catch {
      return null;
    }
  }

  private systemTheme(): Theme {
    const query = this.document.defaultView?.matchMedia?.('(prefers-color-scheme: dark)');
    return query?.matches ? 'dark' : 'light';
  }
}
