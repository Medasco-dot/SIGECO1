import { test, expect } from '@playwright/test';

test.describe('Authentification', () => {
  test("un identifiant sans session valide est redirigé vers la connexion", async ({ page }) => {
    await page.goto('/dossiers');
    await expect(page).toHaveURL(/\/login/);
  });

  test('des identifiants invalides affichent un message d\'erreur et ne redirigent pas', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#identifiant', 'e2e-inexistant');
    await page.fill('#motDePasse', 'mot-de-passe-incorrect');
    await page.click('.submit-button');

    await expect(page.locator('.error-message')).toBeVisible();
    await expect(page.locator('.error-message')).toContainText('invalide');
    await expect(page).toHaveURL(/\/login/);
  });

  test('une connexion valide redirige vers le tableau de bord et affiche l\'identité de l\'utilisateur', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#identifiant', 'juriste');
    await page.fill('#motDePasse', 'Test1234!');
    await page.click('.submit-button');

    await expect(page).toHaveURL(/\/tableau-de-bord/);
    await expect(page.locator('.user-name')).toHaveText('juriste');
    await expect(page.locator('.user-role')).toHaveText('juriste');
  });

  test('la déconnexion invalide la session et renvoie vers la connexion', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#identifiant', 'juriste');
    await page.fill('#motDePasse', 'Test1234!');
    await page.click('.submit-button');
    await expect(page).toHaveURL(/\/tableau-de-bord/);

    await page.click('.user-menu');
    await page.click('.btn-logout');
    await expect(page).toHaveURL(/\/login/);

    // Après déconnexion, une page protégée doit de nouveau exiger une connexion.
    await page.goto('/dossiers');
    await expect(page).toHaveURL(/\/login/);
  });
});
