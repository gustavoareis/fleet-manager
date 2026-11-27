import { defineConfig, devices } from '@playwright/test';

/**
 * End-to-end tests drive the real app: Angular (dev server, which proxies /api and /admin)
 * + Spring Boot + PostgreSQL with the dev seed.
 *
 * Start the backend first (./mvnw spring-boot:run in /backend). The Angular dev server is
 * started here, or reused when it is already running.
 *
 * One worker: the specs share one database, and the booking spec books and cancels real slots.
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : [['list']],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:4200',
    locale: 'pt-BR',
    timezoneId: 'America/Sao_Paulo',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: process.env.E2E_BASE_URL
    ? undefined
    : {
        command: 'npm start',
        cwd: '../frontend',
        url: 'http://localhost:4200',
        reuseExistingServer: true,
        timeout: 180_000,
      },
});
