import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../environments/environment';

export interface Cabinet {
  identifiantCabinet: string;
  nomCabinet: string;
  mail?: string | null;
  telephone?: string | null;
  adresse?: string | null;
}

@Injectable({ providedIn: 'root' })
export class CabinetService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.apiUrl}/api/cabinets`;

  getAll() {
    return this.http.get<Cabinet[]>(this.api);
  }

  getById(identifiantCabinet: string) {
    return this.http.get<Cabinet>(`${this.api}/${identifiantCabinet}`);
  }

  create(c: Cabinet) {
    return this.http.post<Cabinet>(this.api, c);
  }

  update(identifiantCabinet: string, c: Partial<Cabinet>) {
    return this.http.put<Cabinet>(`${this.api}/${identifiantCabinet}`, c);
  }

  delete(identifiantCabinet: string) {
    return this.http.delete<void>(`${this.api}/${identifiantCabinet}`);
  }
}
