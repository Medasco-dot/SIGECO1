import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export interface DossierCabinet {
  numeroDossier: string;
  identifiantCabinet: string;
  nomAvocatReferent?: string;
}

@Injectable({ providedIn: 'root' })
export class DossierCabinetService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/dossiers-cabinets`;

  getAll() {
    return this.http.get<DossierCabinet[]>(this.api);
  }

  getByDossier(numeroDossier: string) {
    return this.http.get<DossierCabinet[]>(`${this.api}/dossier/${numeroDossier}`);
  }

  getById(numeroDossier: string, identifiantCabinet: string) {
    return this.http.get<DossierCabinet>(`${this.api}/${numeroDossier}/${identifiantCabinet}`);
  }

  create(dc: DossierCabinet) {
    return this.http.post<DossierCabinet>(this.api, dc);
  }

  update(numeroDossier: string, identifiantCabinet: string, dc: Partial<DossierCabinet>) {
    return this.http.put<DossierCabinet>(`${this.api}/${numeroDossier}/${identifiantCabinet}`, dc);
  }

  delete(numeroDossier: string, identifiantCabinet: string) {
    return this.http.delete<void>(`${this.api}/${numeroDossier}/${identifiantCabinet}`);
  }
}
