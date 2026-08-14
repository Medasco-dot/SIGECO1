import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';
import type { TypeContentieux, TypeContentieuxNature } from './dossier.service';

@Injectable({ providedIn: 'root' })
export class TypeContentieuxService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/types-contentieux`;

  getAll() {
    return this.http.get<TypeContentieux[]>(this.api);
  }

  getById(numContentieux: number) {
    return this.http.get<TypeContentieux>(`${this.api}/${numContentieux}`);
  }

  create(tc: Omit<TypeContentieux, 'numContentieux'> & { nature: TypeContentieuxNature }) {
    return this.http.post<TypeContentieux>(this.api, tc);
  }

  update(numContentieux: number, tc: Partial<TypeContentieux>) {
    return this.http.put<TypeContentieux>(`${this.api}/${numContentieux}`, tc);
  }

  delete(numContentieux: number) {
    return this.http.delete<void>(`${this.api}/${numContentieux}`);
  }
}
