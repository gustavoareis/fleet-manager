import { Page, expect, test } from '@playwright/test';
import { ACCOUNTS, PASSWORD } from './helpers';

/**
 * The JSP reception module. It has its own session (not the Angular JWT), reached through the
 * dev server's /admin proxy, so these tests exercise the same URLs a user would type.
 */
async function adminLogin(page: Page, email: string): Promise<void> {
  await page.goto('/admin/login');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Senha').fill(PASSWORD);
  await page.getByRole('button', { name: 'Entrar' }).click();
}

test.describe('Módulo administrativo (JSP)', () => {
  test('visitante é redirecionado ao login', async ({ page }) => {
    await page.goto('/admin/consultas');

    await expect(page).toHaveURL(/\/admin\/login/);
  });

  test('recepção entra e vê o painel e as consultas do dia', async ({ page }) => {
    await adminLogin(page, ACCOUNTS.reception);
    await expect(page).toHaveURL(/\/admin$/);

    await page.getByRole('link', { name: 'Consultas do dia' }).first().click();

    await expect(page.getByRole('heading', { level: 1 })).toContainText('Consultas');
    await expect(page.getByRole('table')).toBeVisible();
  });

  test('paciente não entra no módulo administrativo', async ({ page }) => {
    await adminLogin(page, 'paciente@medagenda.local');

    await expect(page.getByRole('heading', { level: 1 })).toContainText('Acesso negado');
  });

  test('senha errada volta ao login com mensagem', async ({ page }) => {
    await page.goto('/admin/login');
    await page.getByLabel('E-mail').fill(ACCOUNTS.reception);
    await page.getByLabel('Senha').fill('errada');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/erro/);
    await expect(page.getByRole('alert')).toBeVisible();
  });

  test('cadastra e exclui uma especialidade, escapando HTML digitado', async ({ page }) => {
    const token = String(Date.now());
    const name = `E2E <b>Teste</b> ${token}`;
    await adminLogin(page, ACCOUNTS.admin);
    await page.goto('/admin/especialidades/nova');

    await page.getByLabel('Nome').fill(name);
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByRole('status')).toHaveText('Especialidade cadastrada.');
    const row = page.getByRole('row', { name: new RegExp(token) });
    // The text is shown literally: the tags were escaped, not rendered
    await expect(row.getByRole('cell').first()).toContainText('<b>Teste</b>');
    expect(await row.locator('b').count()).toBe(0);

    page.once('dialog', (dialog) => dialog.accept());
    await row.getByRole('button', { name: /Excluir/ }).click();
    await expect(page.getByRole('row', { name: new RegExp(token) })).toHaveCount(0);
  });

  test('o link da área da equipe no Angular abre o módulo', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('E-mail').fill(ACCOUNTS.reception);
    await page.getByLabel('Senha', { exact: true }).fill(PASSWORD);
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page).toHaveURL(/\/equipe$/);

    await page.getByRole('link', { name: /módulo administrativo/i }).click();

    await expect(page).toHaveURL(/\/admin\/login/);
  });
});
