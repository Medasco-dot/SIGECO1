import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';
import type { EtapeDossierEtape } from './dossier.service';

export interface EtapeDossier {
  id?: number;
  etape: EtapeDossierEtape;
  dateDebut: string;
  dateFin?: string | null;
  numeroDossier: string;
}

@Injectable({ providedIn: 'root' })
export class EtapeDossierService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/etapes-dossier`;

  getAll() {
    return this.http.get<EtapeDossier[]>(this.api);
  }

  getById(id: number) {
    return this.http.get<EtapeDossier>(`${this.api}/${id}`);
  }

  getByDossier(numeroDossier: string) {
    return this.http.get<EtapeDossier[]>(`${this.api}/dossier/${numeroDossier}`);
  }

  create(e: Omit<EtapeDossier, 'id'>) {
    return this.http.post<EtapeDossier>(this.api, e);
  }

  update(id: number, e: Partial<EtapeDossier>) {
    return this.http.put<EtapeDossier>(`${this.api}/${id}`, e);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
