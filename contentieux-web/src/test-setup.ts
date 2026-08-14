import '@analogjs/vite-plugin-angular/setup-vitest';
import '@angular/compiler';
import { getTestBed } from '@angular/core/testing';
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from '@angular/platform-browser-dynamic/testing';

declare global {
  // eslint-disable-next-line no-var
  var __ngtbinit: boolean | undefined;
}

beforeAll(() => {
  if (!globalThis.__ngtbinit) {
    globalThis.__ngtbinit = true;
    getTestBed().initTestEnvironment(
      BrowserDynamicTestingModule,
      platformBrowserDynamicTesting(),
    );
  }
});
