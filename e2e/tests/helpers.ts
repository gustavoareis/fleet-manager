import { Page, expect } from '@playwright/test';

export const PASSWORD = 'Senha@123';

export const ACCOUNTS = {
  patient: 'maria.silva@medagenda.local',
  doctor: 'medico@medagenda.local',
  reception: 'recepcao@medagenda.local',
  admin: 'admin@medagenda.local',
} as const;

/**
 * The Angular dev server may re-optimize dependencies the first time a page is loaded, which
 * reloads the page in the middle of a test. Visiting the login page once and waiting for the
 * form makes later navigation stable.
 */
export async function loginAs(page: Page, email: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha', { exact: true }).fill(PASSWORD);
  await page.getByRole('button', { name: 'Entrar' }).click();
}

export async function expectHomeAfterLogin(page: Page, path: string): Promise<void> {
  await expect(page).toHaveURL(new RegExp(`${path}$`));
}

/** Next Monday as yyyy-mm-dd: the seeded doctors have availability on Mondays. */
export function nextMonday(): Date {
  const now = new Date();
  const daysAhead = (8 - now.getDay()) % 7 || 7;
  return new Date(now.getFullYear(), now.getMonth(), now.getDate() + daysAhead);
}
