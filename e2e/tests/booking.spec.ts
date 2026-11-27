import { expect, test } from '@playwright/test';
import { ACCOUNTS, loginAs, nextMonday } from './helpers';

/**
 * Patient journey: book -> see it in "my appointments" -> cancel.
 * It cancels what it books, so it can run again on the same database.
 */
test('paciente agenda e cancela uma consulta', async ({ page }) => {
  const monday = nextMonday();
  const dayLabel = monday.toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' });

  await loginAs(page, ACCOUNTS.patient);
  await expect(page).toHaveURL(/\/paciente$/);
  // Warm-up: lets the dev server finish optimizing dependencies before the long flow
  await page.waitForTimeout(1500);
  await page.reload();

  await page.getByRole('link', { name: 'Agendar', exact: true }).click();
  await expect(page).toHaveURL(/\/paciente\/agendar$/);

  // 1. Specialty
  await page.getByRole('option', { name: 'Cardiologia' }).click();
  await page.getByRole('button', { name: 'Próximo' }).first().click();

  // 2. Doctor
  await page.getByRole('option', { name: /Dra. Ana Cardoso/ }).click();
  await page.locator('button.mat-stepper-next:visible:not([disabled])').click();

  // 3. Date: Sundays have no availability, Mondays do
  await page.locator('mat-calendar').waitFor();
  if (monday.getMonth() !== new Date().getMonth()) {
    await page.getByRole('button', { name: /Próximo mês|Next month/ }).click();
  }
  await page.locator(`button[aria-label="${dayLabel}"]`).click();
  await page.locator('button.mat-stepper-next:visible:not([disabled])').click();

  // 4. Time: take the first free slot offered
  const slots = page.getByRole('listbox', { name: 'Horários disponíveis' }).getByRole('option');
  await expect(slots.first()).toBeVisible();
  const time = (await slots.first().textContent())!.trim();
  await slots.first().click();
  await page.locator('button.mat-stepper-next:visible:not([disabled])').click();

  // 5. Confirm
  await page.getByRole('button', { name: 'Confirmar agendamento' }).click();
  await expect(page).toHaveURL(/\/paciente\/consultas$/);
  await expect(page.getByText('Consulta agendada com sucesso!')).toBeVisible();

  const booked = page.locator('.list li', { hasText: `às ${time}` }).filter({ hasText: 'Dra. Ana Cardoso' });
  await expect(booked).toContainText('Agendada');

  // Cancel (more than 24 h ahead, so it is allowed)
  await booked.getByRole('button', { name: /Cancelar consulta com/ }).click();
  await page.getByRole('dialog').getByRole('button', { name: 'Cancelar consulta' }).click();
  await expect(page.getByText('Consulta cancelada.')).toBeVisible();
  await expect(booked).toHaveCount(0);

  await page.getByRole('tab', { name: 'Histórico' }).click();
  await expect(page.getByText('Cancelada').first()).toBeVisible();
});

test('o mesmo horário não pode ser reservado duas vezes', async ({ page, request }) => {
  const monday = nextMonday();
  const iso = `${monday.getFullYear()}-${String(monday.getMonth() + 1).padStart(2, '0')}-${String(monday.getDate()).padStart(2, '0')}`;
  const token = async (email: string) =>
    (await (await request.post('/api/auth/login', { data: { email, password: 'Senha@123' } })).json())
      .accessToken as string;

  const maria = await token(ACCOUNTS.patient);
  const reception = await token(ACCOUNTS.reception);
  const auth = (jwt: string) => ({ Authorization: `Bearer ${jwt}` });

  const doctors = await (await request.get('/api/doctors?name=ana', { headers: auth(maria) })).json();
  const doctorId = doctors.content[0].id as number;
  const free = await (
    await request.get(`/api/doctors/${doctorId}/free-slots?date=${iso}`, { headers: auth(maria) })
  ).json();
  // LocalTime is serialized as "HH:mm" or "HH:mm:ss"; the API wants a full local date-time
  const slot = `${iso}T${String(free.slots[0]).slice(0, 5)}:00`;

  const first = await request.post('/api/appointments', {
    headers: auth(maria),
    data: { doctorId, scheduledAt: slot },
  });
  expect(first.status()).toBe(201);
  const created = await first.json();

  try {
    // Reception tries to put another patient in the same doctor's slot
    const patients = await (await request.get('/api/patients?size=5', { headers: auth(reception) })).json();
    const other = patients.content.find((p: { name: string }) => !p.name.includes('Maria'));
    const second = await request.post('/api/appointments', {
      headers: auth(reception),
      data: { doctorId, scheduledAt: slot, patientId: other.id },
    });
    expect(second.status()).toBe(409);
    expect((await second.json()).detail).toContain('reservado');
  } finally {
    const cancelled = await request.patch(`/api/appointments/${created.id}/cancel`, { headers: auth(maria) });
    expect(cancelled.ok()).toBeTruthy();
  }
  void page;
});
