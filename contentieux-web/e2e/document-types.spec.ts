import { test, expect } from '@playwright/test';

async function loginAsJuriste(page: import('@playwright/test').Page) {
  await page.goto('/login');
  await page.fill('#identifiant', 'juriste');
  await page.fill('#motDePasse', 'Test1234!');
  await page.click('.submit-button');
  await expect(page).toHaveURL(/\/tableau-de-bord/);
}

// Le référentiel des types de document était jusqu'ici une liste figée dans le code backend :
// un nouveau type de contentieux imprévu pouvait arriver sans qu'aucune des pièces qui lui sont
// propres ne figure dans la liste. Ce test verrouille le parcours complet de création/suppression.
test.describe('Référentiel des types de document', () => {
  test('un nouveau type créé apparaît dans les sélecteurs, puis peut être supprimé', async ({ page }) => {
    await loginAsJuriste(page);
    await page.goto('/documents');

    const code = 'type_e2e_' + Date.now();
    await page.fill('input[name="newTypeCode"]', code);
    await page.fill('input[name="newTypeLibelle"]', 'Type E2E de test');
    await page.click('button:has-text("Ajouter type à la liste")');

    await expect(page.locator('.notice')).toContainText('ajouté à la liste');
    await expect(page.locator('table').getByText(code, { exact: true })).toBeVisible();

    // Le sélecteur d'upload de pièce doit immédiatement proposer le nouveau type.
    const typeSelect = page.locator('select[name="typeDocument"]');
    await expect(typeSelect.locator(`option[value="${code}"]`)).toHaveCount(1);

    page.once('dialog', (d) => d.accept());
    await page.locator('tr', { hasText: code }).getByRole('button', { name: 'Supprimer' }).click();

    await expect(page.locator('.notice')).toContainText('supprimé');
    await expect(page.locator('table').getByText(code, { exact: true })).toHaveCount(0);
  });

  test('un type déjà utilisé par un document ne peut pas être supprimé', async ({ page }) => {
    await loginAsJuriste(page);
    await page.goto('/documents');

    const requeteRow = page.locator('tr').filter({ has: page.locator('td', { hasText: /^requete$/ }) });
    page.once('dialog', (d) => d.accept());
    await requeteRow.getByRole('button', { name: 'Supprimer' }).click();

    await expect(page.locator('.notice')).toContainText('ne peut pas être supprimé');
    await expect(requeteRow).toBeVisible();
  });
});
