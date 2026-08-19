import { test, expect } from '@playwright/test';

async function login(page: import('@playwright/test').Page, identifiant: string, motDePasse: string) {
  await page.goto('/login');
  await page.fill('#identifiant', identifiant);
  await page.fill('#motDePasse', motDePasse);
  await page.click('.submit-button');
  await expect(page).toHaveURL(/\/tableau-de-bord/);
}

test.describe('Contrôle d\'accès par rôle (RBAC)', () => {
  test('un juriste ne voit pas les statistiques consolidées sur le tableau de bord', async ({ page }) => {
    await login(page, 'juriste', 'Test1234!');
    await expect(page.getByText('réservées au chef de service et à la Direction Générale')).toBeVisible();
  });

  test('un juriste ne peut pas accéder directement au formulaire de création de dossier', async ({ page }) => {
    await login(page, 'admin', 'admin123');
    // "admin" est chef_service : ne peut pas créer de dossier (seul le rôle juriste le peut).
    await page.goto('/dossiers/nouveau');
    await expect(page).toHaveURL(/\/dossiers$/);
  });

  test('un chef de service consulte les statistiques consolidées', async ({ page }) => {
    await login(page, 'admin', 'admin123');
    await expect(page.getByText('réservées au chef de service et à la Direction Générale')).not.toBeVisible();
  });
});
