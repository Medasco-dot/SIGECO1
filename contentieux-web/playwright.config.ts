import { defineConfig, devices } from '@playwright/test';

/**
 * Suite E2E du parcours contentieux (connexion, création de dossier, RBAC,
 * accessibilité axe-core, responsive). S'exécute contre le déploiement Docker
 * complet (docker compose up -d), sur http://localhost — c'est le même artefact
 * que celui réellement livré, pas un serveur de dev séparé.
 *
 * Comptes attendus par la suite (créés/mis à jour manuellement dans la base
 * lors du test de pré-déploiement du 4 septembre 2026, cf. rapport de test) :
 *   - identifiant "juriste"  / mot de passe "Test1234!" (rôle juriste)
 *   - identifiant "admin"    / mot de passe "admin123"  (rôle chef_service)
 * À reprovisionner si la base est réinitialisée (docker compose down -v).
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
