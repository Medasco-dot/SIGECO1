import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { DossiersComponent } from './dossiers.component';
import { DossierFormComponent } from './dossier-form.component';
import { DossierDetailComponent } from './dossier-detail.component';
import { ReferentielComponent } from './referentiel.component';
import { AudiencesComponent } from './audiences.component';
import { DocumentsComponent } from './documents.component';
import { LoginComponent } from './login.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'tableau-de-bord', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'dossiers', component: DossiersComponent, canActivate: [authGuard] },
  { path: 'dossiers/nouveau', component: DossierFormComponent, canActivate: [authGuard] },
  { path: 'dossiers/:numeroDossier', component: DossierDetailComponent, canActivate: [authGuard] },
  { path: 'parties', component: ReferentielComponent, data: { mode: 'parties' }, canActivate: [authGuard] },
  { path: 'juristes', component: ReferentielComponent, data: { mode: 'juristes' }, canActivate: [authGuard] },
  { path: 'cabinets', component: ReferentielComponent, data: { mode: 'cabinets' }, canActivate: [authGuard] },
  { path: 'utilisateurs', component: ReferentielComponent, data: { mode: 'utilisateurs' }, canActivate: [authGuard] },
  { path: 'audiences', component: AudiencesComponent, canActivate: [authGuard] },
  { path: 'documents', component: DocumentsComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' }
];
