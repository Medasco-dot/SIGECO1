import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export type ImplicationRole = 'demandeur' | 'defendeur';
export type ImplicationLienParente = 'assure' | 'epoux_epouse' | 'enfant' | 'autre_ayant_droit';

export interface Implication {
  numeroDossier: string;
  idPartie: number;
  role: ImplicationRole;
  lienParente?: ImplicationLienParente;
}

@Injectable({ providedIn: 'root' })
export class ImplicationService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/implications`;

  getAll() {
    return this.http.get<Implication[]>(this.api);
  }

  getByDossier(numeroDossier: string) {
    return this.http.get<Implication[]>(`${this.api}/dossier/${numeroDossier}`);
  }

  getById(numeroDossier: string, idPartie: number) {
    return this.http.get<Implication>(`${this.api}/${numeroDossier}/${idPartie}`);
  }

  create(i: Implication) {
    return this.http.post<Implication>(this.api, i);
  }

  update(numeroDossier: string, idPartie: number, i: Partial<Implication>) {
    return this.http.put<Implication>(`${this.api}/${numeroDossier}/${idPartie}`, i);
  }

  delete(numeroDossier: string, idPartie: number) {
    return this.http.delete<void>(`${this.api}/${numeroDossier}/${idPartie}`);
  }
}
