import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export type TypeContentieuxNature =
  | 'pension_retraite'
  | 'pension_reversement'
  | 'acte_carriere'
  | 'marche_public'
  | 'penal'
  | 'autre';

export const NATURES_CONTENTIEUX: TypeContentieuxNature[] = [
  'pension_retraite',
  'pension_reversement',
  'acte_carriere',
  'marche_public',
  'penal',
  'autre'
];

export const NATURE_LABELS: Record<TypeContentieuxNature, string> = {
  pension_retraite: 'Pension retraite',
  pension_reversement: 'Pension de réversion',
  acte_carriere: 'Acte de carrière',
  marche_public: 'Marché public',
  penal: 'Pénal',
  autre: 'Autre contentieux'
};

export type EtapeDossierEtape =
  | 'ouvert'
  | 'en_instruction'
  | 'juge'
  | 'en_appel'
  | 'en_cassation'
  | 'cloture'
  | 'classe_sans_suite';

export const ETAPES_DOSSIER: EtapeDossierEtape[] = [
  'ouvert',
  'en_instruction',
  'juge',
  'en_appel',
  'en_cassation',
  'cloture',
  'classe_sans_suite'
];

export const ETAPE_LABELS: Record<EtapeDossierEtape, string> = {
  ouvert: 'Ouvert',
  en_instruction: 'En instruction',
  juge: 'Juge (première instance)',
  en_appel: 'En appel',
  en_cassation: 'En cassation',
  cloture: 'Clôturé',
  classe_sans_suite: 'Classé sans suite'
};

export interface TypeContentieux {
  numContentieux?: number;
  nature: TypeContentieuxNature;
}

export interface Dossier {
  numeroDossier: string;
  dateOuverture: string;
  resumeAffaire?: string | null;
  observation?: string | null;
  risqueFinancier?: number | null;
  montantReclame?: number | null;
  fraisJustice?: number | null;
  numContentieux?: number | null;
  typeContentieux?: TypeContentieux | null;
  nature?: TypeContentieuxNature | null;
}

export interface DossierSearchCriteria {
  numeroDossier?: string;
  typeContentieux?: TypeContentieuxNature;
  etapeCourante?: EtapeDossierEtape;
  nomPartie?: string;
  dateOuvertureMin?: string;
  dateOuvertureMax?: string;
  risqueFinancierMin?: number;
  risqueFinancierMax?: number;
  matriculeJuriste?: string;
  identifiantCabinet?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class DossierService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/dossiers`;

  getAll() {
    return this.http.get<Dossier[]>(this.api);
  }

  getById(numeroDossier: string) {
    return this.http.get<Dossier>(`${this.api}/${numeroDossier}`);
  }

  exportPdf(numeroDossier: string) {
    return this.http.get(`${this.api}/${numeroDossier}/export/pdf`, { responseType: 'blob' });
  }

  exportExcel(numeroDossier: string) {
    return this.http.get(`${this.api}/${numeroDossier}/export/excel`, { responseType: 'blob' });
  }

  exportWord(numeroDossier: string) {
    return this.http.get(`${this.api}/${numeroDossier}/export/word`, { responseType: 'blob' });
  }

  update(numeroDossier: string, dossier: Partial<Dossier>) {
    return this.http.put<Dossier>(`${this.api}/${numeroDossier}`, dossier);
  }

  rechercherParNumero(valeur: string) {
    return this.http.get<Dossier[]>(`${this.api}/recherche/numero`, { params: { valeur } });
  }

  search(criteria: DossierSearchCriteria, page = 0, size = 10) {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    Object.entries(criteria).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') {
        params = params.set(k, v.toString());
      }
    });
    return this.http.get<Page<Dossier>>(`${this.api}/recherche`, { params });
  }

  create(dossier: Omit<Dossier, 'numeroDossier'>) {
    // Ensure numeroDossier is never sent by stripping it from the payload even if present (defensive)
    const { numeroDossier, ...payload } = dossier as any;
    return this.http.post<Dossier>(this.api, payload);
  }

  remove(numeroDossier: string) {
    return this.http.delete<void>(`${this.api}/${numeroDossier}`);
  }
}
