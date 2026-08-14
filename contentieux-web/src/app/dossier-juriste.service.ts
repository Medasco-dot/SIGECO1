import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export interface DossierJuriste {
  numeroDossier: string;
  matricule: string;
}

@Injectable({ providedIn: 'root' })
export class DossierJuristeService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/dossiers-juristes`;

  getAll() {
    return this.http.get<DossierJuriste[]>(this.api);
  }

  getByDossier(numeroDossier: string) {
    return this.http.get<DossierJuriste[]>(`${this.api}/dossier/${numeroDossier}`);
  }

  getById(numeroDossier: string, matricule: string) {
    return this.http.get<DossierJuriste>(`${this.api}/${numeroDossier}/${matricule}`);
  }

  create(dj: DossierJuriste) {
    return this.http.post<DossierJuriste>(this.api, dj);
  }

  update(numeroDossier: string, matricule: string, dj: Partial<DossierJuriste>) {
    return this.http.put<DossierJuriste>(`${this.api}/${numeroDossier}/${matricule}`, dj);
  }

  delete(numeroDossier: string, matricule: string) {
    return this.http.delete<void>(`${this.api}/${numeroDossier}/${matricule}`);
  }
}
