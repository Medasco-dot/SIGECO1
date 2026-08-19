import { defineConfig, devices } from '@playwright/test';

/**
 * Suite E2E du parcours contentieux (connexion, création de dossier, RBAC).
 * Nécessite le backend Spring Boot démarré sur http://localhost:8082 avec des
 * données de dev (voir README section "Tests E2E") : le serveur Angular est
 * lancé automatiquement ci-dessous si aucune instance n'écoute déjà sur 4200.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    reuseExistingServer: true,
    timeout: 120_000,
  },
});
