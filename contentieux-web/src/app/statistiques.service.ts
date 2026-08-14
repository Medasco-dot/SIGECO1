import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

export type EtapeKey = string;
export type NatureKey = string;

export interface DossierLeger {
  numeroDossier: string;
  typeContentieux?: string | null;
  risqueFinancier?: number | null;
  etapeCourante?: string | null;
}

export interface StatistiquesSynthese {
  totalDossiers: number;
  dossiersOuverts: number;
  dossiersEnAppel: number;
  dossiersEnCassation: number;
  dossiersAlerte: number;
  risqueFinancierCumule?: number | null;
  fraisJusticeCumules?: number | null;
  montantReclameCumule?: number | null;
  repartitionParType: Record<NatureKey, number>;
  repartitionParEtape: Record<EtapeKey, number>;
  top5Risques: DossierLeger[];
}

@Injectable({ providedIn: 'root' })
export class StatistiquesService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/statistiques`;

  synthese() {
    return this.http.get<StatistiquesSynthese>(`${this.base}/synthese`);
  }

  exportPdf() {
    return this.http.get(`${this.base}/export/pdf`, { responseType: 'blob' });
  }

  exportExcel() {
    return this.http.get(`${this.base}/export/excel`, { responseType: 'blob' });
  }

  exportWord() {
    return this.http.get(`${this.base}/export/word`, { responseType: 'blob' });
  }
}

