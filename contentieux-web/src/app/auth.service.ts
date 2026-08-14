import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { tap } from 'rxjs';

export interface AuthUser {
  identifiant: string;
  role: string;
}

export interface LoginRequest {
  identifiant: string;
  motDePasse: string;
}

export interface LoginResponse {
  token: string;
  identifiant: string;
  role: string;
}

const STORAGE_KEY = 'contentieux.auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api`;
  private readonly token = signal<string | null>(null);

  readonly currentUser = signal<AuthUser | null>(null);
  readonly isAuthenticated = computed(() => !!this.token());

  constructor() {
    const stored = this.readStoredSession();
    if (stored) {
      this.token.set(stored.token);
      this.currentUser.set({ identifiant: stored.identifiant, role: stored.role });
    }
  }

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, credentials).pipe(
      tap((response) => {
        this.token.set(response.token);
        this.currentUser.set({ identifiant: response.identifiant, role: response.role });
        this.persistSession(response);
      })
    );
  }

  logout() {
    this.token.set(null);
    this.currentUser.set(null);
    sessionStorage.removeItem(STORAGE_KEY);
  }

  private persistSession(response: LoginResponse) {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(response));
  }

  private readStoredSession(): LoginResponse | null {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as LoginResponse;
    } catch {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }

  getToken() {
    return this.token();
  }

  hasRole(roles: string[]) {
    const role = this.currentUser()?.role?.toLowerCase();
    return !!role && roles.map((entry) => entry.toLowerCase()).includes(role);
  }

  canMutate() {
    // Créer/modifier/supprimer un dossier et ses éléments rattachés (étapes, audiences,
    // documents, implications, cabinets) est réservé au juriste côté backend.
    // Seule l'association juriste↔dossier fait exception (cf. canAssignJuriste()).
    return this.hasRole(['juriste']);
  }

  canAssignJuriste() {
    return this.hasRole(['chef_service']);
  }

  canViewStatistiques() {
    return this.hasRole(['chef_service', 'direction_generale']);
  }
}
