import { test, expect } from '@playwright/test';

async function loginAsJuriste(page: import('@playwright/test').Page) {
  await page.goto('/login');
  await page.fill('#identifiant', 'juriste');
  await page.fill('#motDePasse', 'Test1234!');
  await page.click('.submit-button');
  await expect(page).toHaveURL(/\/tableau-de-bord/);
}

test.describe('Parcours dossier (rôle juriste)', () => {
  test('création puis consultation d\'un nouveau dossier', async ({ page }) => {
    await loginAsJuriste(page);

    await page.goto('/dossiers');
    await page.click('a[routerLink="/dossiers/nouveau"]');
    await expect(page).toHaveURL(/\/dossiers\/nouveau/);

    await page.fill('input[formcontrolname="dateOuverture"]', '2026-08-10');
    await page.selectOption('select[formcontrolname="nature"]', 'pension_retraite');
    await page.fill('input[formcontrolname="montantReclame"]', '500000');

    const creationResponse = page.waitForResponse((r) => r.url().includes('/api/dossiers') && r.request().method() === 'POST');
    await page.click('button[type="submit"]');
    const response = await creationResponse;
    expect(response.status()).toBe(201);
    const created = await response.json();
    const numeroDossier = created.numeroDossier as string;
    expect(numeroDossier).toBeTruthy();

    await expect(page).toHaveURL(/\/dossiers$/);

    await page.goto(`/dossiers/${numeroDossier}`);
    await expect(page.locator('h1')).toHaveText(numeroDossier);
    await expect(page.getByText('Pension retraite').first()).toBeVisible();

    // Le test crée un vrai dossier en base : on le supprime pour ne pas polluer les données
    // de démonstration à chaque exécution de la suite.
    await page.request.delete(`http://localhost:8082/api/dossiers/${numeroDossier}`, {
      headers: { Authorization: `Bearer ${await page.evaluate(() => JSON.parse(sessionStorage.getItem('contentieux.auth') || '{}').token)}` },
    });
  });

  test('le formulaire de création rejette une soumission sans date d\'ouverture', async ({ page }) => {
    await loginAsJuriste(page);
    await page.goto('/dossiers/nouveau');

    await expect(page.locator('button[type="submit"]')).toBeDisabled();
    await page.selectOption('select[formcontrolname="nature"]', 'penal');
    await expect(page.locator('button[type="submit"]')).toBeDisabled();
  });
});
