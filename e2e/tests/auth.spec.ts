import { expect, test } from '@playwright/test';
import { ACCOUNTS, PASSWORD, expectHomeAfterLogin, loginAs } from './helpers';

test.describe('Login e controle de acesso', () => {
  const homes = [
    ['paciente', ACCOUNTS.patient, '/paciente'],
    ['médico', ACCOUNTS.doctor, '/medico'],
    ['recepção', ACCOUNTS.reception, '/equipe'],
    ['administrador', ACCOUNTS.admin, '/equipe'],
  ] as const;

  for (const [profile, email, home] of homes) {
    test(`${profile} entra e vai para a própria área`, async ({ page }) => {
      await loginAs(page, email);
      await expectHomeAfterLogin(page, home);
    });
  }

  test('senha errada mostra erro e mantém o usuário na tela de login', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('E-mail').fill(ACCOUNTS.patient);
    await page.getByLabel('Senha', { exact: true }).fill('errada');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page.getByRole('alert')).toContainText(/e-mail ou senha/i);
    await expect(page).toHaveURL(/\/login/);
  });

  test('visitante é enviado ao login e volta à página pedida depois de entrar', async ({ page }) => {
    await page.goto('/paciente/consultas');
    await expect(page).toHaveURL(/\/login\?returnUrl=%2Fpaciente%2Fconsultas/);

    await page.getByLabel('E-mail').fill(ACCOUNTS.patient);
    await page.getByLabel('Senha', { exact: true }).fill(PASSWORD);
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/paciente\/consultas$/);
  });

  test('paciente não acessa a área da equipe', async ({ page }) => {
    await loginAs(page, ACCOUNTS.patient);
    await expectHomeAfterLogin(page, '/paciente');

    await page.goto('/equipe');

    await expect(page).toHaveURL(/acesso-negado/);
  });

  test('sair encerra a sessão e protege as páginas novamente', async ({ page }) => {
    await loginAs(page, ACCOUNTS.patient);
    await expectHomeAfterLogin(page, '/paciente');

    await page.getByRole('button', { name: /Menu do usuário/ }).click();
    await page.getByRole('menuitem', { name: 'Sair' }).click();
    await expect(page).toHaveURL(/\/login/);

    await page.goto('/paciente');
    await expect(page).toHaveURL(/\/login/);
  });
});
