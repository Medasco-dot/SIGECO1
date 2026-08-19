import '@angular/compiler';
/// <reference types="vitest" />
import '@analogjs/vite-plugin-angular/setup-vitest';
import { vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService, LoginResponse } from './auth.service';

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

describe('AuthService', () => {
  let service: AuthService;
  let httpPostSpy: ReturnType<typeof vi.fn>;

  const reponseJuriste: LoginResponse = { token: 'jeton-juriste', identifiant: 'amed', role: 'juriste' };
  const reponseChefService: LoginResponse = { token: 'jeton-chef', identifiant: 'basile', role: 'chef_service' };
  const reponseDirection: LoginResponse = { token: 'jeton-direction', identifiant: 'celine', role: 'direction_generale' };

  beforeEach(() => {
    sessionStorage.clear();
    httpPostSpy = vi.fn(() => of(reponseJuriste));
    mockHttp.ref.current = { post: httpPostSpy } as unknown as HttpClient;
    service = new AuthService();
  });

  it('n’est pas authentifié avant connexion', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('login() authentifie l’utilisateur et mémorise le rôle', () => {
    service.login({ identifiant: 'amed', motDePasse: 'x' }).subscribe();

    expect(service.isAuthenticated()).toBe(true);
    expect(service.currentUser()?.role).toBe('juriste');
    expect(service.getToken()).toBe('jeton-juriste');
  });

  it('logout() efface la session', () => {
    service.login({ identifiant: 'amed', motDePasse: 'x' }).subscribe();
    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('un échec de connexion ne authentifie pas l’utilisateur', () => {
    httpPostSpy.mockReturnValue(throwError(() => new Error('401')));

    service.login({ identifiant: 'amed', motDePasse: 'faux' }).subscribe({ error: () => {} });

    expect(service.isAuthenticated()).toBe(false);
  });

  describe('Droits par rôle (RBAC)', () => {
    it('canMutate() : vrai uniquement pour un juriste', () => {
      httpPostSpy.mockReturnValue(of(reponseJuriste));
      service.login({ identifiant: 'amed', motDePasse: 'x' }).subscribe();
      expect(service.canMutate()).toBe(true);

      httpPostSpy.mockReturnValue(of(reponseChefService));
      service.login({ identifiant: 'basile', motDePasse: 'x' }).subscribe();
      expect(service.canMutate()).toBe(false);
    });

    it('canAssignJuriste() : vrai uniquement pour un chef de service', () => {
      httpPostSpy.mockReturnValue(of(reponseChefService));
      service.login({ identifiant: 'basile', motDePasse: 'x' }).subscribe();
      expect(service.canAssignJuriste()).toBe(true);

      httpPostSpy.mockReturnValue(of(reponseJuriste));
      service.login({ identifiant: 'amed', motDePasse: 'x' }).subscribe();
      expect(service.canAssignJuriste()).toBe(false);
    });

    it('canViewStatistiques() : vrai pour chef de service et direction générale, faux pour juriste', () => {
      httpPostSpy.mockReturnValue(of(reponseChefService));
      service.login({ identifiant: 'basile', motDePasse: 'x' }).subscribe();
      expect(service.canViewStatistiques()).toBe(true);

      httpPostSpy.mockReturnValue(of(reponseDirection));
      service.login({ identifiant: 'celine', motDePasse: 'x' }).subscribe();
      expect(service.canViewStatistiques()).toBe(true);

      httpPostSpy.mockReturnValue(of(reponseJuriste));
      service.login({ identifiant: 'amed', motDePasse: 'x' }).subscribe();
      expect(service.canViewStatistiques()).toBe(false);
    });
  });

  it('la session survit à une nouvelle instance du service (sessionStorage)', () => {
    service.login({ identifiant: 'amed', motDePasse: 'x' }).subscribe();

    const nouvelleInstance = new AuthService();

    expect(nouvelleInstance.isAuthenticated()).toBe(true);
    expect(nouvelleInstance.currentUser()?.identifiant).toBe('amed');
  });
});
