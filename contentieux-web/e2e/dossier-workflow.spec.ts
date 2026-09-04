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

    // On ne lit que le statut de la reponse (disponible des la reception des en-tetes), jamais
    // son corps JSON : Angular navigue vers /dossiers immediatement apres la creation, et Chrome
    // peut deja avoir libere le buffer CDP du corps de la reponse au moment ou le test tenterait
    // de le lire ("Response body is not available for a response that was navigated away from")
    // - un artefact de timing reste possible meme en attendant le corps immediatement (constate
    // lors du test de pre-deploiement du 4 septembre 2026). Le numero du dossier cree est retrouve
    // ensuite via l'interface (recherche par date d'ouverture), comme le ferait un utilisateur.
    const [response] = await Promise.all([
      page.waitForResponse((r) => r.url().includes('/api/dossiers') && r.request().method() === 'POST'),
      page.click('button[type="submit"]'),
    ]);
    expect(response.status()).toBe(201);

    await expect(page).toHaveURL(/\/dossiers$/);

    await page.fill('input[formcontrolname="dateOuvertureMin"]', '2026-08-10');
    await page.fill('input[formcontrolname="dateOuvertureMax"]', '2026-08-10');
    await page.click('button:has-text("Rechercher")');
    const ligne = page.locator('tbody tr', { hasText: 'Pension retraite' }).first();
    await expect(ligne).toBeVisible();
    const numeroDossier = (await ligne.locator('td').first().innerText()).trim();
    expect(numeroDossier).toMatch(/^DOS-\d{4}-\d{4}$/);

    await page.goto(`/dossiers/${numeroDossier}`);
    await expect(page.locator('h1')).toHaveText(numeroDossier);
    await expect(page.getByText('Pension retraite').first()).toBeVisible();

    // Le test crée un vrai dossier en base : on le supprime pour ne pas polluer les données
    // de démonstration à chaque exécution de la suite. Utilise la même baseURL que le reste
    // du test (config Playwright) plutôt qu'un port codé en dur, pour rester valide aussi bien
    // en dev (ng serve + backend 8082) qu'en conditions réelles de déploiement (Docker, port 80).
    await page.request.delete(`/api/dossiers/${numeroDossier}`, {
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
