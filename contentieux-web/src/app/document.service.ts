import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export type DocumentType = string;

export interface DocumentTypeOption {
  code: string;
  libelle: string;
}

export interface Document {
  idDocument?: number;
  typeDocument?: DocumentType;
  dateAjout?: string;
  fichier?: string;
  numeroDossier: string;
}

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/documents`;
  private readonly typeApi = `${environment.apiUrl}/api/document-types`;

  getAll() {
    return this.http.get<Document[]>(this.api);
  }

  getById(idDocument: number) {
    return this.http.get<Document>(`${this.api}/${idDocument}`);
  }

  getByDossier(numeroDossier: string) {
    return this.http.get<Document[]>(`${this.api}/dossier/${numeroDossier}`);
  }

  getDocumentTypes() {
    return this.http.get<Array<string | DocumentTypeOption>>(this.typeApi);
  }

  createDocumentType(payload: { code: string; libelle: string }) {
    return this.http.post<DocumentTypeOption>(this.typeApi, payload);
  }

  rechercherParType(type: DocumentType) {
    return this.http.get<Document[]>(`${this.api}/recherche/type`, { params: { type } });
  }

  rechercherParDate(debut: string, fin: string) {
    let params = new HttpParams().set('debut', debut).set('fin', fin);
    return this.http.get<Document[]>(`${this.api}/recherche/date`, { params });
  }

  upload(formData: FormData) {
    return this.http.post<Document>(`${this.api}/upload`, formData);
  }

  create(d: Omit<Document, 'idDocument'>) {
    return this.http.post<Document>(this.api, d);
  }

  update(idDocument: number, d: Partial<Document>) {
    return this.http.put<Document>(`${this.api}/${idDocument}`, d);
  }

  delete(idDocument: number) {
    return this.http.delete<void>(`${this.api}/${idDocument}`);
  }
}
