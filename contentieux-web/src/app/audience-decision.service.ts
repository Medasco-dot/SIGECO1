import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export type AudienceTypeEtape = 'premiere_instance' | 'appel' | 'cassation';
export type AudienceNatureDecision = 'jugement' | 'arret' | 'ordonnance';
export type AudienceIssueCarfo = 'favorable' | 'defavorable' | 'partiellement_favorable';

export interface AudienceDecision {
  numAudienceDecision?: number;
  date: string;
  lieuAudience?: string;
  typeEtape: AudienceTypeEtape;
  natureDecision?: AudienceNatureDecision;
  resumeDecision?: string;
  issuePourCarfo?: AudienceIssueCarfo;
  montantObtenu?: number;
  montantDu?: number;
  fraisJustice?: number;
  etapeDossierId: number;
  numeroDossier?: string;
}

@Injectable({ providedIn: 'root' })
export class AudienceDecisionService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/audiences-decisions`;

  getAll() {
    return this.http.get<AudienceDecision[]>(this.api);
  }

  getById(numAudienceDecision: number) {
    return this.http.get<AudienceDecision>(`${this.api}/${numAudienceDecision}`);
  }

  getByDossier(numeroDossier: string) {
    return this.http.get<AudienceDecision[]>(`${this.api}/dossier/${numeroDossier}`);
  }

  rechercherParType(type: AudienceTypeEtape) {
    return this.http.get<AudienceDecision[]>(`${this.api}/recherche/type`, { params: { type } });
  }

  rechercherParDate(debut: string, fin: string) {
    let params = new HttpParams().set('debut', debut).set('fin', fin);
    return this.http.get<AudienceDecision[]>(`${this.api}/recherche/date`, { params });
  }

  rechercherParIssue(issue: AudienceIssueCarfo) {
    return this.http.get<AudienceDecision[]>(`${this.api}/recherche/issue`, { params: { issue } });
  }

  create(a: Omit<AudienceDecision, 'numAudienceDecision'>) {
    return this.http.post<AudienceDecision>(this.api, a);
  }

  update(numAudienceDecision: number, a: Partial<AudienceDecision>) {
    return this.http.put<AudienceDecision>(`${this.api}/${numAudienceDecision}`, a);
  }

  delete(numAudienceDecision: number) {
    return this.http.delete<void>(`${this.api}/${numAudienceDecision}`);
  }
}
