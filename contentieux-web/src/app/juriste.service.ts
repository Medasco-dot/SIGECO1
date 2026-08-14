import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export type JuristeSpecialite =
  | 'pension_retraite'
  | 'pension_reversement'
  | 'acte_carriere'
  | 'marche_public'
  | 'penal'
  | 'polyvalent';

export interface Juriste {
  matricule: string;
  nom: string;
  prenoms: string;
  specialite: JuristeSpecialite;
}

@Injectable({ providedIn: 'root' })
export class JuristeService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/juristes`;

  getAll() {
    return this.http.get<Juriste[]>(this.api);
  }

  getById(matricule: string) {
    return this.http.get<Juriste>(`${this.api}/${matricule}`);
  }

  create(j: Juriste) {
    return this.http.post<Juriste>(this.api, j);
  }

  update(matricule: string, j: Partial<Juriste>) {
    return this.http.put<Juriste>(`${this.api}/${matricule}`, j);
  }

  delete(matricule: string) {
    return this.http.delete<void>(`${this.api}/${matricule}`);
  }
}
