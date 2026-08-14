import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export type PartieStatutMatrimonial = 'marie' | 'celibataire' | 'divorce' | 'veuf';

export interface Partie {
  id?: number;
  nom: string;
  prenom: string;
  numeroCnib?: string;
  statutMatrimonial?: PartieStatutMatrimonial;
}

@Injectable({ providedIn: 'root' })
export class PartieService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/parties`;

  getAll() {
    return this.http.get<Partie[]>(this.api);
  }

  getById(id: number) {
    return this.http.get<Partie>(`${this.api}/${id}`);
  }

  create(p: Omit<Partie, 'id'>) {
    return this.http.post<Partie>(this.api, p);
  }

  update(id: number, p: Partial<Partie>) {
    return this.http.put<Partie>(`${this.api}/${id}`, p);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
