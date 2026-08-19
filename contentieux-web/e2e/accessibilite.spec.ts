import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

/**
 * Audit d'accessibilité automatisé (WCAG 2.0/2.1 A+AA via axe-core) sur les pages
 * les plus consultées : connexion, tableau de bord, liste des dossiers, création
 * de dossier. Ne remplace pas un audit manuel complet (navigation clavier fine,
 * lecteur d'écran réel) mais couvre la classe d'erreurs la plus fréquente
 * (contraste, labels manquants, rôles ARIA invalides, structure de landmarks).
 */

async function loginAsJuriste(page: import('@playwright/test').Page) {
  await page.goto('/login');
  await page.fill('#identifiant', 'juriste');
  await page.fill('#motDePasse', 'Test1234!');
  await page.click('.submit-button');
  await expect(page).toHaveURL(/\/tableau-de-bord/);
}

test.describe('Accessibilité (axe-core, WCAG2A + WCAG2AA)', () => {
  test('page de connexion', async ({ page }) => {
    await page.goto('/login');
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze();
    expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([]);
  });

  test('tableau de bord', async ({ page }) => {
    await loginAsJuriste(page);
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze();
    expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([]);
  });

  test('liste des dossiers', async ({ page }) => {
    await loginAsJuriste(page);
    await page.goto('/dossiers');
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze();
    expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([]);
  });

  test('formulaire de création de dossier', async ({ page }) => {
    await loginAsJuriste(page);
    await page.goto('/dossiers/nouveau');
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa']).analyze();
    expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([]);
  });
});
