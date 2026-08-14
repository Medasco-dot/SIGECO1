import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
@Injectable({providedIn:'root'}) export class ReferentielService { private http=inject(HttpClient); private base=`${environment.apiUrl}/api`;
  liste<T>(ressource:string){return this.http.get<T[]>(`${this.base}/${ressource}`)}
  creer<T>(ressource:string, valeur:T){return this.http.post<T>(`${this.base}/${ressource}`,valeur)}
  update<T>(ressource:string, id:any, valeur:T){return this.http.put<T>(`${this.base}/${ressource}/${id}`, valeur)}
  supprimer<T>(ressource:string, id:any){return this.http.delete<void>(`${this.base}/${ressource}/${id}`)}
}
