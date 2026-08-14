import '@angular/compiler';
/// <reference types="vitest" />
import '@analogjs/vite-plugin-angular/setup-vitest';
import { vi } from 'vitest';
import { of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { DossierService, Dossier, DossierSearchCriteria, Page } from './dossier.service';
import { environment } from '../environments/environment';

const mockHttp = vi.hoisted(() => {
  const ref: { current: HttpClient | undefined } = { current: undefined };
  return { ref };
});

vi.mock('@angular/core', async (importOriginal) => {
  const actual: typeof import('@angular/core') = await importOriginal();
  return {
    ...actual,
    inject: ((token: unknown, ..._rest: unknown[]): unknown => {
      if (token === HttpClient) {
        return mockHttp.ref.current as HttpClient;
      }
      throw new Error(`inject() non mocké pour: ${String(token)}`);
    }) as typeof import('@angular/core').inject,
  };
});

describe('DossierService', () => {
  let service: DossierService;
  let httpGetSpy: ReturnType<typeof vi.fn>;
  let httpPostSpy: ReturnType<typeof vi.fn>;
  let httpPutSpy: ReturnType<typeof vi.fn>;

  const API: string = `${environment.apiUrl}/api/dossiers`;

  beforeEach(() => {
    httpGetSpy = vi.fn(() => of(null as unknown));
    httpPostSpy = vi.fn(() => of(null as unknown));
    httpPutSpy = vi.fn(() => of(null as unknown));

    mockHttp.ref.current = {
      get: httpGetSpy,
      post: httpPostSpy,
      put: httpPutSpy,
    } as unknown as HttpClient;

    service = new DossierService();
  });

  it('devrait être créé', () => {
    expect(service).toBeTruthy();
  });

  it('getAll() envoie GET /api/dossiers', () => {
    const dossiers: Dossier[] = [
      { numeroDossier: 'DOS-2025-0001', dateOuverture: '2025-01-01' },
    ];
    httpGetSpy.mockReturnValue(of(dossiers));

    let recu: Dossier[] | undefined;
    service.getAll().subscribe((res: Dossier[]) => {
      recu = res;
    });

    expect(httpGetSpy).toHaveBeenCalledWith(API);
    expect(recu!.length).toBe(1);
    expect(recu![0].numeroDossier).toBe('DOS-2025-0001');
  });

  it('getById() envoie GET /api/dossiers/{numero}', () => {
    const attendu: Dossier = { numeroDossier: 'DOS-1', dateOuverture: '2025-01-01' };
    httpGetSpy.mockReturnValue(of(attendu));

    let recu: Dossier | undefined;
    service.getById('DOS-1').subscribe((res: Dossier) => {
      recu = res;
    });

    expect(httpGetSpy).toHaveBeenCalledWith(`${API}/DOS-1`);
    expect(recu!.numeroDossier).toBe('DOS-1');
  });

  it('create() envoie POST /api/dossiers avec body sans numeroDossier', () => {
    const body: Omit<Dossier, 'numeroDossier'> = {
      dateOuverture: '2025-02-02',
    };
    const returned: Dossier = {
      numeroDossier: 'DOS-2025-0002',
      dateOuverture: '2025-02-02',
    };
    httpPostSpy.mockReturnValue(of(returned));

    let recu: Dossier | undefined;
    service.create(body).subscribe((res: Dossier) => {
      recu = res;
    });

    expect(httpPostSpy).toHaveBeenCalledTimes(1);
    const args = httpPostSpy.mock.calls[0];
    expect(args[0]).toBe(API);
    expect((args[1] as Omit<Dossier, 'numeroDossier'>).dateOuverture).toBe('2025-02-02');
    expect((args[1] as Omit<Dossier, 'numeroDossier'>).numeroDossier).toBeUndefined();
    expect(recu!.numeroDossier).toBe(returned.numeroDossier);
  });

  it('update() envoie PUT /api/dossiers/{id} avec payload Partial<Dossier>', () => {
    const modifications: Partial<Dossier> = { observation: 'OK' };
    httpPutSpy.mockReturnValue(of(null as unknown));

    service.update('DOS-1', modifications).subscribe();

    expect(httpPutSpy).toHaveBeenCalledTimes(1);
    const args = httpPutSpy.mock.calls[0];
    expect(args[0]).toBe(`${API}/DOS-1`);
    expect((args[1] as Partial<Dossier>).observation).toBe('OK');
  });

  it('search() envoie GET /recherche avec criteria, page, size', () => {
    const criteria: DossierSearchCriteria = { typeContentieux: 'pension_retraite' };
    const page: Page<Dossier> = {
      content: [{ numeroDossier: 'DOS-1', dateOuverture: '2025-01-01' }],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
    };
    httpGetSpy.mockReturnValue(of(page));

    let recu: Page<Dossier> | undefined;
    service.search(criteria, 0, 10).subscribe((res: Page<Dossier>) => {
      recu = res;
    });

    expect(httpGetSpy).toHaveBeenCalledTimes(1);
    const args = httpGetSpy.mock.calls[0];
    expect(args[0]).toBe(`${API}/recherche`);
    const params = args[1] as { params: { get: (k: string) => string | null } };
    expect(params.params.get('typeContentieux')).toBe('pension_retraite');
    expect(params.params.get('page')).toBe('0');
    expect(params.params.get('size')).toBe('10');
    expect(recu!.content.length).toBe(1);
    expect(recu!.totalElements).toBe(1);
  });

  // exportCsv / exportData removed: these endpoints are not part of the dossier creation perimeter.
  // Tests for exportCsv/exportData removed accordingly.

  it('rechercherParNumero() envoie GET /recherche/numero?valeur=...', () => {
    httpGetSpy.mockReturnValue(of([] as Dossier[]));

    service.rechercherParNumero('DOS-1').subscribe();

    expect(httpGetSpy).toHaveBeenCalledTimes(1);
    const args = httpGetSpy.mock.calls[0];
    expect((args[0] as string).startsWith(`${API}/recherche/numero`)).toBe(true);
    const opts = args[1] as { params: Record<string, string> };
    expect(opts.params.valeur).toBe('DOS-1');
  });
});
