import { test, expect } from '@playwright/test';

async function loginAsJuriste(page: import('@playwright/test').Page) {
  await page.goto('/login');
  await page.fill('#identifiant', 'juriste');
  await page.fill('#motDePasse', 'Test1234!');
  await page.click('.submit-button');
  await expect(page).toHaveURL(/\/tableau-de-bord/);
}

test.describe('Navigation responsive', () => {
  test('sur desktop, le menu est affiché en ligne sans bouton hamburger', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await loginAsJuriste(page);

    await expect(page.locator('.nav-toggle')).toBeHidden();
    await expect(page.locator('.main-nav')).toBeVisible();
    await expect(page.locator('.main-nav a', { hasText: 'Documents' })).toBeVisible();
  });

  test('sous 960px, le menu est replié derrière un bouton hamburger accessible', async ({ page }) => {
    await page.setViewportSize({ width: 820, height: 900 });
    await loginAsJuriste(page);

    const toggle = page.locator('.nav-toggle');
    const nav = page.locator('#main-nav');

    await expect(toggle).toBeVisible();
    await expect(nav).toBeHidden();
    await expect(toggle).toHaveAttribute('aria-expanded', 'false');

    await toggle.click();
    await expect(nav).toBeVisible();
    await expect(toggle).toHaveAttribute('aria-expanded', 'true');

    // Les 7 liens doivent être atteignables sans défilement caché.
    const documentsLink = nav.getByRole('link', { name: 'Documents' });
    await expect(documentsLink).toBeVisible();
    const box = await documentsLink.boundingBox();
    expect(box).not.toBeNull();
    expect(box!.x + box!.width).toBeLessThanOrEqual(820);

    await documentsLink.click();
    await expect(page).toHaveURL(/\/documents/);
    await expect(nav).toBeHidden();
  });
});
