import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import {
  AudienceDecisionService,
  AudienceDecision,
  AudienceTypeEtape,
  AudienceIssueCarfo,
} from './audience-decision.service';
import { DossierService, Dossier, ETAPE_LABELS } from './dossier.service';
import { EtapeDossierService, EtapeDossier } from './etape-dossier.service';

const TYPE_ETAPE_LABELS: Record<AudienceTypeEtape, string> = {
  premiere_instance: 'Première instance',
  appel: 'Appel',
  cassation: 'Cassation',
};

const ISSUE_LABELS: Record<AudienceIssueCarfo, string> = {
  favorable: 'Favorable',
  defavorable: 'Défavorable',
  partiellement_favorable: 'Partiellement favorable',
};

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="page-intro">
      <div>
        <p class="eyebrow">Procédure</p>
        <h1>Audiences et décisions</h1>
        <p class="intro">Planifiez les audiences et gardez un historique clair des décisions.</p>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Recherche</h2>
          <p class="muted">Filtrez les audiences par type, période ou issue.</p>
        </div>
      </div>
      <div class="form-grid">
        <label>Type d'étape
          <select [(ngModel)]="searchType" name="searchType">
            <option value="">Tous</option>
            <option value="premiere_instance">Première instance</option>
            <option value="appel">Appel</option>
            <option value="cassation">Cassation</option>
          </select>
        </label>
        <label>Date début<input type="date" [(ngModel)]="searchDateDebut" name="searchDateDebut"/></label>
        <label>Date fin<input type="date" [(ngModel)]="searchDateFin" name="searchDateFin"/></label>
        <label>Issue CARFO
          <select [(ngModel)]="searchIssue" name="searchIssue">
            <option value="">Toutes</option>
            <option value="favorable">Favorable</option>
            <option value="defavorable">Défavorable</option>
            <option value="partiellement_favorable">Partiellement favorable</option>
          </select>
        </label>
      </div>
      <div class="form-actions">
        <button (click)="rechercher()">Rechercher</button>
        <button type="button" class="button secondary" (click)="resetRecherche()">Tout afficher</button>
      </div>
      @if(searchMessage()){
        <p class="notice">{{searchMessage()}}</p>
      }
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Enregistrer ou modifier une audience</h2>
          <p class="muted">Saisissez les informations de l'audience ou modifiez une entrée existante.</p>
        </div>
      </div>

      <div class="form-grid">
        <label>Identifiant (modification)<input [(ngModel)]="selectedId" name="selectedId" type="number"/></label>
        <label>Rechercher dossier<input [(ngModel)]="numeroRecherche" name="numeroRecherche" placeholder="Saisir un numéro ou fragment"/></label>
        <label style="align-self:end"><button type="button" (click)="rechercherDossiers()">Rechercher</button></label>
        <label>Choisir dossier<select [(ngModel)]="numeroDossier" name="numeroDossier" (ngModelChange)="onDossierChange($event)">
          <option value="">-- sélectionner --</option>
          @for(d of dossierResults(); track d.numeroDossier){
            <option [value]="d.numeroDossier">{{ d.numeroDossier }} - {{ d.typeContentieux?.nature || '' }}</option>
          }
        </select></label>
        <label>Étape du dossier<select [(ngModel)]="etapeDossierId" name="etapeDossierId">
          <option value="">-- sélectionner --</option>
          @for(e of etapesDuDossier(); track e.id){
            <option [value]="e.id">{{ ETAPE_LABELS[e.etape] }} ({{ e.dateDebut }})</option>
          }
        </select></label>
        <label>Date<input type="date" [(ngModel)]="date" name="date"/></label>
        <label>Lieu / juridiction<input [(ngModel)]="lieuAudience" name="lieuAudience"/></label>
        <label>Type étape<select [(ngModel)]="typeEtape" name="typeEtape">
          <option value="premiere_instance">Première instance</option>
          <option value="appel">Appel</option>
          <option value="cassation">Cassation</option>
        </select></label>
      </div>

      <div class="form-actions">
        <button (click)="enregistrer()">Enregistrer</button>
        <button type="button" class="button secondary" (click)="vider()">Nouveau</button>
      </div>

      @if(message()){
        <p class="notice">{{message()}}</p>
      }
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Audiences enregistrées</h2>
          <p class="muted">Historique des audiences.</p>
        </div>
      </div>
      <div class="table-shell">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>N° Dossier</th>
              <th>Date</th>
              <th>Lieu</th>
              <th>Étape</th>
              <th>Décision</th>
              <th>Issue CARFO</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for(a of audiences();track a.numAudienceDecision){
              <tr>
                <td>{{a.numAudienceDecision}}</td>
                <td>{{a.numeroDossier || '-'}}</td>
                <td>{{a.date}}</td>
                <td>{{a.lieuAudience || '-'}}</td>
                <td>{{a.typeEtape ? TYPE_ETAPE_LABELS[a.typeEtape] : '-'}}</td>
                <td>{{a.natureDecision || '-'}}</td>
                <td>{{a.issuePourCarfo ? ISSUE_LABELS[a.issuePourCarfo] : '-'}}</td>
                <td>
                  <button class="link" (click)="charger(a)">Modifier</button>
                  <button class="link danger" (click)="supprimer(a.numAudienceDecision!)">Supprimer</button>
                </td>
              </tr>
            }
            @empty{
              <tr><td colspan="8" class="empty">Aucune audience.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </section>
  `,
  styles: [
    `
      .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 16px;
      }

      .form-actions {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-top: 18px;
      }

      @media (max-width: 900px) {
        .form-grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class AudiencesComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly audSvc = inject(AudienceDecisionService);
  private readonly dossierSvc = inject(DossierService);
  private readonly etapeSvc = inject(EtapeDossierService);
  private readonly api = `${environment.apiUrl}/api/audiences-decisions`;

  readonly TYPE_ETAPE_LABELS = TYPE_ETAPE_LABELS;
  readonly ISSUE_LABELS = ISSUE_LABELS;
  readonly ETAPE_LABELS = ETAPE_LABELS;

  selectedId?: number | null = null;
  numeroRecherche = '';
  dossierResults = signal<Dossier[]>([]);
  numeroDossier = '';
  etapesDuDossier = signal<EtapeDossier[]>([]);
  etapeDossierId: number | '' = '';
  date = '';
  lieuAudience = '';
  typeEtape: AudienceTypeEtape = 'premiere_instance';
  message = signal('');
  audiences = signal<AudienceDecision[]>([]);

  searchType: AudienceTypeEtape | '' = '';
  searchDateDebut = '';
  searchDateFin = '';
  searchIssue: AudienceIssueCarfo | '' = '';
  searchMessage = signal('');

  ngOnInit() {
    this.chargerToutes();
  }

  private chargerToutes() {
    this.audSvc.getAll().subscribe({
      next: (list) => this.audiences.set(list),
      error: () => this.message.set('Impossible de charger les audiences.'),
    });
  }

  rechercher() {
    this.searchMessage.set('');
    if (this.searchType) {
      this.audSvc.rechercherParType(this.searchType).subscribe({
        next: (list) => this.audiences.set(list),
        error: () => this.searchMessage.set('Erreur lors de la recherche par type.'),
      });
    } else if (this.searchDateDebut && this.searchDateFin) {
      this.audSvc.rechercherParDate(this.searchDateDebut, this.searchDateFin).subscribe({
        next: (list) => this.audiences.set(list),
        error: () => this.searchMessage.set('Erreur lors de la recherche par date.'),
      });
    } else if (this.searchIssue) {
      this.audSvc.rechercherParIssue(this.searchIssue).subscribe({
        next: (list) => this.audiences.set(list),
        error: () => this.searchMessage.set('Erreur lors de la recherche par issue.'),
      });
    } else {
      this.searchMessage.set('Veuillez saisir au moins un critère de recherche.');
    }
  }

  resetRecherche() {
    this.searchType = '';
    this.searchDateDebut = '';
    this.searchDateFin = '';
    this.searchIssue = '';
    this.searchMessage.set('');
    this.chargerToutes();
  }

  rechercherDossiers() {
    if (!this.numeroRecherche || !this.numeroRecherche.trim()) {
      this.message.set('Saisissez un numéro ou un fragment pour rechercher.');
      return;
    }
    this.dossierSvc.rechercherParNumero(this.numeroRecherche.trim()).subscribe({
      next: (list) => {
        this.dossierResults.set(list);
        this.message.set(list.length ? `${list.length} dossier(s) trouvé(s).` : 'Aucun dossier trouvé.');
      },
      error: () => this.message.set('Erreur lors de la recherche de dossiers.'),
    });
  }

  onDossierChange(numeroDossier: string) {
    this.etapeDossierId = '';
    this.etapesDuDossier.set([]);
    if (!numeroDossier) return;
    this.etapeSvc.getByDossier(numeroDossier).subscribe({
      next: (etapes) => this.etapesDuDossier.set(etapes),
      error: () => this.message.set('Impossible de charger les étapes de ce dossier.'),
    });
  }

  charger(a: AudienceDecision) {
    this.selectedId = a.numAudienceDecision;
    this.numeroDossier = a.numeroDossier || '';
    this.etapeDossierId = a.etapeDossierId;
    this.date = a.date || '';
    this.lieuAudience = a.lieuAudience || '';
    this.typeEtape = a.typeEtape || 'premiere_instance';
    if (a.numeroDossier) this.onDossierChange(a.numeroDossier);
    this.message.set(`Édition de l'audience ${a.numAudienceDecision}.`);
  }

  vider() {
    this.selectedId = null;
    this.numeroDossier = '';
    this.etapeDossierId = '';
    this.etapesDuDossier.set([]);
    this.date = '';
    this.lieuAudience = '';
    this.typeEtape = 'premiere_instance';
    this.message.set('');
  }

  enregistrer() {
    if (!this.date || !this.typeEtape || !this.etapeDossierId) {
      this.message.set('Date, type d\'étape et étape du dossier sont obligatoires.');
      return;
    }
    const payload: Partial<AudienceDecision> = {
      date: this.date,
      lieuAudience: this.lieuAudience,
      typeEtape: this.typeEtape,
      etapeDossierId: Number(this.etapeDossierId),
    };
    const req = this.selectedId
      ? this.audSvc.update(this.selectedId, payload)
      : this.audSvc.create(payload as Omit<AudienceDecision, 'numAudienceDecision'>);
    req.subscribe({
      next: () => {
        this.message.set(this.selectedId ? 'Audience mise à jour.' : 'Audience enregistrée.');
        this.vider();
        this.chargerToutes();
      },
      error: (err) => this.message.set("L'enregistrement a échoué : " + (err?.error?.message || err?.statusText || 'erreur inconnue')),
    });
  }

  supprimer(num?: number | null) {
    if (!num) return;
    if (!confirm('Supprimer cette audience ?')) return;
    this.audSvc.delete(num).subscribe({
      next: () => {
        this.chargerToutes();
        this.message.set('Audience supprimée.');
      },
      error: (err) => {
        console.error('Erreur suppression audience', err);
        this.message.set('Impossible de supprimer l\'audience.');
      }
    });
  }
}
