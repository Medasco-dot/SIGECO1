import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import {
  Dossier, DossierService, TypeContentieuxNature, EtapeDossierEtape,
  NATURE_LABELS, ETAPES_DOSSIER, ETAPE_LABELS,
} from './dossier.service';
import { EtapeDossier, EtapeDossierService } from './etape-dossier.service';
import { AudienceDecision, AudienceDecisionService, AudienceTypeEtape, AudienceNatureDecision, AudienceIssueCarfo } from './audience-decision.service';
import { Document, DocumentService, DocumentType, DocumentTypeOption } from './document.service';
import { Implication, ImplicationService, ImplicationRole, ImplicationLienParente } from './implication.service';
import { DossierJuriste, DossierJuristeService } from './dossier-juriste.service';
import { DossierCabinet, DossierCabinetService } from './dossier-cabinet.service';
import { Partie, PartieService } from './partie.service';
import { Juriste, JuristeService } from './juriste.service';
import { Cabinet, CabinetService } from './cabinet.service';
import { AuthService } from './auth.service';

type TabKey = 'infos' | 'etapes' | 'audiences' | 'documents' | 'implications' | 'juristes' | 'cabinets';

const TABS: { key: TabKey; label: string }[] = [
  { key: 'infos', label: 'Infos' },
  { key: 'etapes', label: 'Étapes' },
  { key: 'audiences', label: 'Audiences' },
  { key: 'documents', label: 'Documents' },
  { key: 'implications', label: 'Implications' },
  { key: 'juristes', label: 'Juristes' },
  { key: 'cabinets', label: 'Cabinets' },
];

const DOC_LABELS: Record<DocumentType, string> = {
  requete: 'Requête',
  piece_justificative: 'Pièce justificative',
  pv_audience: 'PV d\'audience',
  releve_general_service: 'Relevé général de service',
  indice: 'Indice',
  acte_carriere: 'Acte carrière',
  assignation: 'Assignation',
  convocation: 'Convocation',
  decision_justice: 'Décision de justice',
};

const TYPE_ETAPES: AudienceTypeEtape[] = ['premiere_instance', 'appel', 'cassation'];
const TYPE_ETAPE_LABELS: Record<AudienceTypeEtape, string> = {
  premiere_instance: 'Première instance',
  appel: 'Appel',
  cassation: 'Cassation',
};

const NATURE_DECISIONS: AudienceNatureDecision[] = ['jugement', 'arret', 'ordonnance'];
const NATURE_DECISION_LABELS: Record<AudienceNatureDecision, string> = {
  jugement: 'Jugement',
  arret: 'Arrêt',
  ordonnance: 'Ordonnance',
};

const ISSUES: AudienceIssueCarfo[] = ['favorable', 'defavorable', 'partiellement_favorable'];
const ISSUE_LABELS: Record<AudienceIssueCarfo, string> = {
  favorable: 'Favorable',
  defavorable: 'Défavorable',
  partiellement_favorable: 'Partiellement favorable',
};

const ROLES: ImplicationRole[] = ['demandeur', 'defendeur'];
const ROLE_LABELS: Record<ImplicationRole, string> = {
  demandeur: 'Demandeur',
  defendeur: 'Défendeur',
};

const LIENS: ImplicationLienParente[] = ['assure', 'epoux_epouse', 'enfant', 'autre_ayant_droit'];
const LIEN_LABELS: Record<ImplicationLienParente, string> = {
  assure: 'Assuré',
  epoux_epouse: 'Époux(se)',
  enfant: 'Enfant',
  autre_ayant_droit: 'Autre ayant droit',
};

@Component({
  standalone: true,
  imports: [DecimalPipe, RouterLink, ReactiveFormsModule],
  template: `
    <header>
      <div>
        <p class="eyebrow">Dossier</p>
        <h1>{{ dossier()?.numeroDossier || 'Chargement...' }}</h1>
        <div class="infos-strip">
          <span><strong>Type :</strong> {{ dossier()?.typeContentieux?.nature ? NATURE_LABELS[dossier()!.typeContentieux!.nature] : '—' }}</span>
        </div>
      </div>
      <div class="actions-header">
        <a routerLink="/dossiers">← Retour aux dossiers</a>
        <div class="actions-inline">
          <button class="button" (click)="onExportPdf()">Exporter PDF</button>
          <button class="button" (click)="onExportExcel()">Exporter Excel</button>
          <button class="button" (click)="onExportWord()">Exporter Word</button>
          @if (canMutateDossier()) {
            <button class="button danger small" [disabled]="!canDelete()" (click)="supprimerDossier()" [title]="deleteTooltip()">{{ canDelete() ? 'Supprimer le dossier' : 'Suppression impossible' }}</button>
          }
        </div>
      </div>
      @if(dossier()){
        <div class="related-counts">
          <small>Relations&nbsp;: {{ documents()?.length || 0 }} document(s) · {{ audiences()?.length || 0 }} audience(s) · {{ implications()?.length || 0 }} implication(s) · {{ dossierJuristes()?.length || 0 }} juriste(s) · {{ dossierCabinets()?.length || 0 }} cabinet(s) · {{ etapes()?.length || 0 }} étape(s)</small>
        </div>
      }

    </header>

    <nav class="tabs">
      @for(t of TABS; track t.key){
        <button [class.active]="tab() === t.key" (click)="tab.set(t.key)">{{ t.label }}</button>
      }
    </nav>

    @if(tab() === 'infos'){
      <section class="panel">
        <div class="panel-head">
          <h2>Informations du dossier</h2>
          @if (canMutateDossier()) {
            <button class="button" (click)="editInfos.set(!editInfos())">{{ editInfos() ? 'Annuler' : 'Modifier' }}</button>
          }
        </div>
        @if(dossier()){
          @if(editInfos()){
            <form class="form" [formGroup]="infosForm" (ngSubmit)="saveInfos()">
              <div class="grid-2">
                <label>Numéro dossier<input formControlName="numeroDossier" readonly/></label>
                <label>Date d'ouverture<input type="date" formControlName="dateOuverture"/></label>
              </div>
              <label>Type de contentieux
                <select formControlName="nature">
                  <option value="pension_retraite">{{ NATURE_LABELS.pension_retraite }}</option>
                  <option value="pension_reversement">{{ NATURE_LABELS.pension_reversement }}</option>
                  <option value="acte_carriere">{{ NATURE_LABELS.acte_carriere }}</option>
                  <option value="marche_public">{{ NATURE_LABELS.marche_public }}</option>
                  <option value="penal">{{ NATURE_LABELS.penal }}</option>
                  <option value="autre">{{ NATURE_LABELS.autre }}</option>
                </select>
              </label>
              <div class="grid-3">
                <label>Montant réclamé<input type="number" formControlName="montantReclame"/></label>
                <label>Risque financier<input type="number" formControlName="risqueFinancier"/></label>
                <label>Frais de justice<input type="number" formControlName="fraisJustice"/></label>
              </div>
              <label>Résumé de l'affaire<textarea formControlName="resumeAffaire" rows="3"></textarea></label>
              <label>Observation<textarea formControlName="observation" rows="3"></textarea></label>
              <button class="primary" [disabled]="infosForm.invalid">Enregistrer</button>
            </form>
          } @else {
            <dl class="dl-grid">
              <dt>Numéro</dt><dd><strong>{{ dossier()!.numeroDossier }}</strong></dd>
              <dt>Ouverture</dt><dd>{{ dossier()!.dateOuverture }}</dd>
              <dt>Type</dt><dd>{{ dossier()!.typeContentieux?.nature ? NATURE_LABELS[dossier()!.typeContentieux!.nature] : '—' }}</dd>
              <dt>Montant réclamé</dt><dd>{{ (dossier()!.montantReclame||0) | number }} FCFA</dd>
              <dt>Risque financier</dt><dd>{{ (dossier()!.risqueFinancier||0) | number }} FCFA</dd>
              <dt>Frais de justice</dt><dd>{{ (dossier()!.fraisJustice||0) | number }} FCFA</dd>
              <dt>Résumé</dt><dd>{{ dossier()!.resumeAffaire || '—' }}</dd>
              <dt>Observation</dt><dd>{{ dossier()!.observation || '—' }}</dd>
            </dl>
          }
        }
      </section>
    }

    @if(tab() === 'etapes'){
      <section class="panel">
        <div class="panel-head">
          <div><h2>Étapes du dossier</h2><p>Historique complet de la réception à la clôture.</p></div>
          @if (canMutateDossier()) {
            <button class="button" (click)="toggleEtapeForm()">{{ showEtapeForm() ? 'Annuler' : '+ Nouvelle étape' }}</button>
          }
        </div>
        @if(showEtapeForm() && !editingEtapeId()){
          <form class="form" [formGroup]="etapeForm" (ngSubmit)="saveEtape()">
            <div class="grid-3">
              <label>Étape
                <select formControlName="etape">
                  @for(e of ETAPES_DOSSIER; track e){<option [value]="e">{{ ETAPE_LABELS[e] }}</option>}
                </select>
              </label>
              <label>Date début<input type="date" formControlName="dateDebut"/></label>
              <label>Date fin<input type="date" formControlName="dateFin"/></label>
            </div>
            <button class="primary" [disabled]="etapeForm.invalid">Ajouter</button>
          </form>
        }
        @if(editingEtapeId()){
          <form class="form" [formGroup]="etapeForm" (ngSubmit)="updateEtape()">
            <div class="notice info">Modification de l'étape #{{ editingEtapeId() }}</div>
            <div class="grid-3">
              <label>Étape
                <select formControlName="etape">
                  @for(e of ETAPES_DOSSIER; track e){<option [value]="e">{{ ETAPE_LABELS[e] }}</option>}
                </select>
              </label>
              <label>Date début<input type="date" formControlName="dateDebut"/></label>
              <label>Date fin<input type="date" formControlName="dateFin"/></label>
            </div>
            <div class="row">
              <button class="primary" [disabled]="etapeForm.invalid">Enregistrer</button>
              <button type="button" class="button" (click)="cancelEtapeEdit()">Annuler</button>
            </div>
          </form>
        }
        <table>
          <thead>
            <tr>
              <th>Étape</th><th>Date début</th><th>Date fin</th>
              @if (canMutateDossier()) { <th></th> }
            </tr>
          </thead>
          <tbody>
            @for(e of etapes(); track e.id){
              <tr>
                <td><strong>{{ ETAPE_LABELS[e.etape] }}</strong></td>
                <td>{{ e.dateDebut }}</td>
                <td>{{ e.dateFin || '—' }}</td>
                @if (canMutateDossier()) {
                  <td>
                    <button class="link" (click)="editEtape(e)">Modifier</button>
                    <button class="link danger" (click)="deleteEtape(e.id!)">Supprimer</button>
                  </td>
                }
              </tr>
            } @empty {<tr><td [attr.colspan]="canMutateDossier() ? 4 : 3" class="empty">Aucune étape.</td></tr>}
          </tbody>
        </table>
      </section>
    }

    @if(tab() === 'audiences'){
      <section class="panel">
        <div class="panel-head"><div><h2>Audiences et décisions</h2><p>Traçabilité première instance → appel → cassation.</p></div>
          @if (canMutateDossier()) {
            <button class="button" (click)="toggleAudForm()">{{ showAudForm() ? 'Annuler' : '+ Nouvelle audience' }}</button>
          }
        </div>
        @if(showAudForm() && !editingAudId()){
          <form class="form" [formGroup]="audForm" (ngSubmit)="saveAud()">
            <div class="grid-2">
              <label>Date<input type="date" formControlName="date"/></label>
              <label>Lieu<input formControlName="lieuAudience"/></label>
            </div>
            <div class="grid-2">
              <label>Étape du dossier
                <select formControlName="etapeDossierId">
                  <option value="">-- sélectionner --</option>
                  @for(e of etapes(); track e.id){<option [value]="e.id">{{ ETAPE_LABELS[e.etape] }} ({{ e.dateDebut }})</option>}
                </select>
              </label>
              <label>&nbsp;</label>
            </div>
            <div class="grid-2">
              <label>Type étape
                <select formControlName="typeEtape">
                  @for(t of TYPE_ETAPES; track t){<option [value]="t">{{ TYPE_ETAPE_LABELS[t] }}</option>}
                </select>
              </label>
              <label>Nature décision
                <select formControlName="natureDecision">
                  <option value="">—</option>
                  @for(n of NATURE_DECISIONS; track n){<option [value]="n">{{ NATURE_DECISION_LABELS[n] }}</option>}
                </select>
              </label>
            </div>
            <div class="grid-2">
              <label>Issue pour CARFO
                <select formControlName="issuePourCarfo">
                  <option value="">—</option>
                  @for(i of ISSUES; track i){<option [value]="i">{{ ISSUE_LABELS[i] }}</option>}
                </select>
              </label>
              <label>&nbsp;</label>
            </div>
            <label>Résumé de décision<textarea formControlName="resumeDecision" rows="2"></textarea></label>
            <div class="grid-3">
              <label>Montant obtenu<input type="number" formControlName="montantObtenu"/></label>
              <label>Montant dû<input type="number" formControlName="montantDu"/></label>
              <label>Frais de justice<input type="number" formControlName="fraisJustice"/></label>
            </div>
            <button class="primary" [disabled]="audForm.invalid">Ajouter</button>
          </form>
        }
        @if(editingAudId()){
          <form class="form" [formGroup]="audForm" (ngSubmit)="updateAud()">
            <div class="notice info">Modification de l'audience #{{ editingAudId() }}</div>
            <div class="grid-2">
              <label>Date<input type="date" formControlName="date"/></label>
              <label>Lieu<input formControlName="lieuAudience"/></label>
            </div>
            <div class="grid-2">
              <label>Étape du dossier
                <select formControlName="etapeDossierId">
                  <option value="">-- sélectionner --</option>
                  @for(e of etapes(); track e.id){<option [value]="e.id">{{ ETAPE_LABELS[e.etape] }} ({{ e.dateDebut }})</option>}
                </select>
              </label>
              <label>&nbsp;</label>
            </div>
            <div class="grid-2">
              <label>Type étape
                <select formControlName="typeEtape">
                  @for(t of TYPE_ETAPES; track t){<option [value]="t">{{ TYPE_ETAPE_LABELS[t] }}</option>}
                </select>
              </label>
              <label>Nature décision
                <select formControlName="natureDecision">
                  <option value="">—</option>
                  @for(n of NATURE_DECISIONS; track n){<option [value]="n">{{ NATURE_DECISION_LABELS[n] }}</option>}
                </select>
              </label>
            </div>
            <div class="grid-2">
              <label>Issue pour CARFO
                <select formControlName="issuePourCarfo">
                  <option value="">—</option>
                  @for(i of ISSUES; track i){<option [value]="i">{{ ISSUE_LABELS[i] }}</option>}
                </select>
              </label>
              <label>&nbsp;</label>
            </div>
            <label>Résumé de décision<textarea formControlName="resumeDecision" rows="2"></textarea></label>
            <div class="grid-3">
              <label>Montant obtenu<input type="number" formControlName="montantObtenu"/></label>
              <label>Montant dû<input type="number" formControlName="montantDu"/></label>
              <label>Frais de justice<input type="number" formControlName="fraisJustice"/></label>
            </div>
            <div class="row">
              <button class="primary" [disabled]="audForm.invalid">Enregistrer</button>
              <button type="button" class="button" (click)="cancelAudEdit()">Annuler</button>
            </div>
          </form>
        }
        <table>
          <thead><tr><th>Date</th><th>Type</th><th>Lieu</th><th>Décision</th><th>Issue</th>
            @if(canMutateDossier()){ <th></th> }
          </tr></thead>
          <tbody>
            @for(a of audiences(); track a.numAudienceDecision){
              <tr>
                <td>{{ a.date }}</td>
                <td>{{ TYPE_ETAPE_LABELS[a.typeEtape] }}</td>
                <td>{{ a.lieuAudience || '—' }}</td>
                <td>{{ a.natureDecision ? NATURE_DECISION_LABELS[a.natureDecision] : '—' }}</td>
                <td>{{ a.issuePourCarfo ? ISSUE_LABELS[a.issuePourCarfo] : '—' }}</td>
                @if(canMutateDossier()){
                  <td>
                    <button class="link" (click)="editAud(a)">Modifier</button>
                    <button class="link danger" (click)="deleteAud(a.numAudienceDecision!)">Supprimer</button>
                  </td>
                }
              </tr>
            } @empty {<tr><td [attr.colspan]="canMutateDossier() ? 6 : 5" class="empty">Aucune audience.</td></tr>}
          </tbody>
        </table>
      </section>
    }

    @if(tab() === 'documents'){
      <section class="panel">
        <div class="panel-head"><div><h2>Documents</h2></div>
          @if (canMutateDossier()) {
            <button class="button" (click)="toggleDocForm()">{{ showDocForm() ? 'Annuler' : '+ Nouveau document' }}</button>
          }
        </div>
        @if(showDocForm() && !editingDocId()){
          <form class="form" [formGroup]="docForm" (ngSubmit)="saveDoc()">
            <div class="grid-2">
              <label>Type
                <select formControlName="typeDocument">
                  <option value="">—</option>
                  @for(t of documentTypeOptions(); track t.code){<option [value]="t.code">{{ t.libelle }}</option>}
                </select>
              </label>
              <label>Date d'ajout<input type="date" formControlName="dateAjout"/></label>
            </div>
            <label class="file-picker">
              <span>{{ selectedDocFile() ? selectedDocFile()!.name : 'Choisir un fichier à joindre' }}</span>
              <input type="file" (change)="choisirDocument($event)" />
            </label>
            <button class="primary" [disabled]="docForm.invalid">Ajouter</button>
          </form>
        }
        @if(editingDocId()){
          <form class="form" [formGroup]="docForm" (ngSubmit)="updateDoc()">
            <div class="notice info">Modification du document #{{ editingDocId() }}</div>
            <div class="grid-2">
              <label>Type
                <select formControlName="typeDocument">
                  <option value="">—</option>
                  @for(t of documentTypeOptions(); track t.code){<option [value]="t.code">{{ t.libelle }}</option>}
                </select>
              </label>
              <label>Date d'ajout<input type="date" formControlName="dateAjout"/></label>
            </div>
            <label class="file-picker">
              <span>{{ selectedDocFile() ? selectedDocFile()!.name : 'Choisir un nouveau fichier à joindre' }}</span>
              <input type="file" (change)="choisirDocument($event)" />
            </label>
            <div class="row">
              <button class="primary" [disabled]="docForm.invalid">Enregistrer</button>
              <button type="button" class="button" (click)="cancelDocEdit()">Annuler</button>
            </div>
          </form>
        }
        <table>
          <thead><tr><th>Type</th><th>Date</th><th>Fichier</th>
            @if(canMutateDossier()){ <th></th> }
          </tr></thead>
          <tbody>
            @for(d of documents(); track d.idDocument){
              <tr>
                <td>{{ d.typeDocument ? typeLabel(d.typeDocument) : '—' }}</td>
                <td>{{ d.dateAjout || '—' }}</td>
                <td>{{ d.fichier || '—' }}</td>
                @if(canMutateDossier()){
                  <td>
                    <button class="link" (click)="editDoc(d)">Modifier</button>
                    <button class="link danger" (click)="deleteDoc(d.idDocument!)">Supprimer</button>
                  </td>
                }
              </tr>
            } @empty {<tr><td [attr.colspan]="canMutateDossier() ? 4 : 3" class="empty">Aucun document.</td></tr>}
          </tbody>
        </table>
      </section>
    }

    @if(tab() === 'implications'){
      <section class="panel">
        <div class="panel-head"><div><h2>Parties impliquées</h2></div>
          @if(canMutateDossier()){
            <button class="button" (click)="toggleImpForm()">{{ showImpForm() ? 'Annuler' : '+ Nouvelle implication' }}</button>
          }
        </div>
        @if(showImpForm() && !editingImpKey()){
          <form class="form" [formGroup]="impForm" (ngSubmit)="saveImp()">
            <div class="grid-3">
              <label>Partie
                <select formControlName="idPartie">
                  @for(p of parties(); track p.id){<option [value]="p.id">{{ p.nom }} {{ p.prenom }}</option>}
                </select>
              </label>
              <label>Rôle
                <select formControlName="role">
                  @for(r of ROLES; track r){<option [value]="r">{{ ROLE_LABELS[r] }}</option>}
                </select>
              </label>
              <label>Lien de parenté
                <select formControlName="lienParente">
                  <option value="">—</option>
                  @for(l of LIENS; track l){<option [value]="l">{{ LIEN_LABELS[l] }}</option>}
                </select>
              </label>
            </div>
            <button class="primary" [disabled]="impForm.invalid">Ajouter</button>
          </form>
        }
        @if(editingImpKey()){
          <form class="form" [formGroup]="impForm" (ngSubmit)="updateImp()">
            <div class="notice info">Modification de l'implication : {{ partyName(editingImpKey()!.idPartie) }} ({{ editingImpKey()!.numeroDossier }})</div>
            <div class="grid-3">
              <label>Partie
                <select formControlName="idPartie">
                  @for(p of parties(); track p.id){<option [value]="p.id">{{ p.nom }} {{ p.prenom }}</option>}
                </select>
              </label>
              <label>Rôle
                <select formControlName="role">
                  @for(r of ROLES; track r){<option [value]="r">{{ ROLE_LABELS[r] }}</option>}
                </select>
              </label>
              <label>Lien de parenté
                <select formControlName="lienParente">
                  <option value="">—</option>
                  @for(l of LIENS; track l){<option [value]="l">{{ LIEN_LABELS[l] }}</option>}
                </select>
              </label>
            </div>
            <div class="row">
              <button class="primary" [disabled]="impForm.invalid">Enregistrer</button>
              <button type="button" class="button" (click)="cancelImpEdit()">Annuler</button>
            </div>
          </form>
        }
        <table>
          <thead><tr><th>Partie</th><th>Rôle</th><th>Lien</th>
            @if(canMutateDossier()){ <th></th> }
          </tr></thead>
          <tbody>
            @for(imp of implications(); track imp.idPartie){
              <tr>
                <td>{{ partyName(imp.idPartie) }}</td>
                <td>{{ ROLE_LABELS[imp.role] }}</td>
                <td>{{ imp.lienParente ? LIEN_LABELS[imp.lienParente] : '—' }}</td>
                @if(canMutateDossier()){
                  <td>
                    <button class="link" (click)="editImp(imp)">Modifier</button>
                    <button class="link danger" (click)="deleteImp(imp)">Supprimer</button>
                  </td>
                }
              </tr>
            } @empty {<tr><td [attr.colspan]="canMutateDossier() ? 4 : 3" class="empty">Aucune implication.</td></tr>}
          </tbody>
        </table>
      </section>
    }

    @if(tab() === 'juristes'){
      <section class="panel">
        <div class="panel-head">
          <div><h2>Juristes associés</h2></div>
          @if (canAssignJuriste()) {
            <button class="button" (click)="toggleDJForm()">{{ showDJForm() ? 'Annuler' : '+ Associer juriste' }}</button>
          }
        </div>
        @if(showDJForm() && !editingDJKey()){
          <form class="form" [formGroup]="djForm" (ngSubmit)="saveDJ()">
            <label>Juriste
              <select formControlName="matricule">
                @for(j of juristes(); track j.matricule){<option [value]="j.matricule">{{ j.matricule }} — {{ j.nom }} {{ j.prenoms }}</option>}
              </select>
            </label>
            <button class="primary" [disabled]="djForm.invalid">Associer</button>
          </form>
        }
        @if(editingDJKey()){
          <form class="form" [formGroup]="djForm" (ngSubmit)="updateDJ()">
            <div class="notice info">Modification de l'affectation juriste</div>
            <label>Juriste
              <select formControlName="matricule">
                @for(j of juristes(); track j.matricule){<option [value]="j.matricule">{{ j.matricule }} — {{ j.nom }} {{ j.prenoms }}</option>}
              </select>
            </label>
            <div class="row">
              <button class="primary" [disabled]="djForm.invalid">Enregistrer</button>
              <button type="button" class="button" (click)="cancelDJEdit()">Annuler</button>
            </div>
          </form>
        }
        <table>
          <thead><tr><th>Matricule</th><th>Nom</th><th>Prénoms</th><th>Spécialité</th>
            <th></th>
          </tr></thead>
          <tbody>
            @for(dj of dossierJuristes(); track dj.matricule){
              <tr>
                <td>{{ dj.matricule }}</td>
                <td>{{ juristeInfo(dj.matricule)?.nom || '—' }}</td>
                <td>{{ juristeInfo(dj.matricule)?.prenoms || '—' }}</td>
                <td>{{ juristeInfo(dj.matricule)?.specialite || '—' }}</td>
                <td>
                  @if (canAssignJuriste()) {
                    <button class="link" (click)="editDJ(dj)">Modifier</button>
                    <button class="link danger" (click)="deleteDJ(dj)">Désassocier</button>
                  }
                </td>
              </tr>
            } @empty {<tr><td colspan="5" class="empty">Aucun juriste associé.</td></tr>}
          </tbody>
        </table>
      </section>
    }

    @if(tab() === 'cabinets'){
      <section class="panel">
        <div class="panel-head">
          <div><h2>Cabinets d'avocats associés</h2></div>
          @if (canMutateDossier()) {
            <button class="button" (click)="toggleDCForm()">{{ showDCForm() ? 'Annuler' : '+ Associer cabinet' }}</button>
          }
        </div>
        @if(showDCForm() && !editingDCKey()){
          <form class="form" [formGroup]="dcForm" (ngSubmit)="saveDC()">
            <div class="grid-2">
              <label>Cabinet
                <select formControlName="identifiantCabinet">
                  @for(c of cabinets(); track c.identifiantCabinet){<option [value]="c.identifiantCabinet">{{ c.identifiantCabinet }} — {{ c.nomCabinet }}</option>}
                </select>
              </label>
              <label>Avocat référent<input formControlName="nomAvocatReferent"/></label>
            </div>
            <button class="primary" [disabled]="dcForm.invalid">Associer</button>
          </form>
        }
        @if(editingDCKey()){
          <form class="form" [formGroup]="dcForm" (ngSubmit)="updateDC()">
            <div class="notice info">Modification de l'affectation cabinet</div>
            <div class="grid-2">
              <label>Cabinet
                <select formControlName="identifiantCabinet">
                  @for(c of cabinets(); track c.identifiantCabinet){<option [value]="c.identifiantCabinet">{{ c.identifiantCabinet }} — {{ c.nomCabinet }}</option>}
                </select>
              </label>
              <label>Avocat référent<input formControlName="nomAvocatReferent"/></label>
            </div>
            <div class="row">
              <button class="primary" [disabled]="dcForm.invalid">Enregistrer</button>
              <button type="button" class="button" (click)="cancelDCEdit()">Annuler</button>
            </div>
          </form>
        }
        <table>
          <thead><tr><th>Cabinet</th><th>Avocat référent</th><th>Contact</th>
            @if (canMutateDossier()) { <th></th> }
          </tr></thead>
          <tbody>
            @for(dc of dossierCabinets(); track dc.identifiantCabinet){
              <tr>
                <td>{{ cabinetInfo(dc.identifiantCabinet)?.nomCabinet || dc.identifiantCabinet }}</td>
                <td>{{ dc.nomAvocatReferent || '—' }}</td>
                <td>{{ cabinetInfo(dc.identifiantCabinet)?.telephone || cabinetInfo(dc.identifiantCabinet)?.mail || '—' }}</td>
                @if (canMutateDossier()) {
                  <td>
                    <button class="link" (click)="editDC(dc)">Modifier</button>
                    <button class="link danger" (click)="deleteDC(dc)">Désassocier</button>
                  </td>
                }
              </tr>
            } @empty {<tr><td [attr.colspan]="canMutateDossier() ? 4 : 3" class="empty">Aucun cabinet associé.</td></tr>}
          </tbody>
        </table>
      </section>
    }
  `,
  styles: [`
    header { display:flex; justify-content: space-between; align-items: flex-end; gap: 16px; flex-wrap: wrap; }
    .infos-strip { display:flex; gap: 14px; flex-wrap: wrap; margin-top: 8px; }
    .infos-strip span { color: #415161; font-size: 13px; }
    .actions-header { display: flex; flex-direction: column; gap: 6px; align-items: flex-end; }
    .actions-inline { align-items: center; gap: 8px; flex-wrap: wrap; }
    .small-input { min-height: 34px; padding: 0 10px; border: 1px solid #bcd0df; border-radius: 8px; background: #fff; }
    .button.small { padding: 6px 12px; font-size: 13px; }
    .button.danger { background: #b91c1c; border-color: #991b1b; }
    .badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 11px; font-weight: 700; }
    .sub-list { margin:0; padding-left: 18px; font-size: 13px; color: #405061; }
    .sub-list li { margin-bottom: 2px; }
    .muted.inline { display:inline-block; margin-top: 4px; }
    .muted  { color: #7a8c9a; }
    .related-counts { margin-top: 8px; }
    .related-counts small { color: #566a7a; }
    .notice.info { background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 10px 14px; border-radius: 10px; margin-bottom: 14px; font-size: 13px; }
    .link.danger { color: #b91c1c; }
    .dl-grid { display:grid; grid-template-columns: 220px 1fr; gap: 8px 18px; }
    .dl-grid dt { font-weight: 600; color: #566a7a; margin: 0; }
    .dl-grid dd { margin: 0; }
    @media (max-width: 860px) { .dl-grid { grid-template-columns: 1fr; } }
  `]
})
export class DossierDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly dossierSvc = inject(DossierService);
  private readonly etapeSvc = inject(EtapeDossierService);
  private readonly audSvc = inject(AudienceDecisionService);
  private readonly docSvc = inject(DocumentService);
  private readonly impSvc = inject(ImplicationService);
  private readonly djSvc = inject(DossierJuristeService);
  private readonly dcSvc = inject(DossierCabinetService);
  private readonly partieSvc = inject(PartieService);
  private readonly juristeSvc = inject(JuristeService);
  private readonly cabinetSvc = inject(CabinetService);
  private readonly authService = inject(AuthService);

  readonly TABS = TABS;
  readonly NATURE_LABELS = NATURE_LABELS;
  readonly ETAPES_DOSSIER: EtapeDossierEtape[] = ETAPES_DOSSIER;
  readonly ETAPE_LABELS = ETAPE_LABELS;
  readonly documentTypeOptions = signal<DocumentTypeOption[]>([
    { code: 'requete', libelle: 'Requête' },
    { code: 'piece_justificative', libelle: 'Pièce justificative' },
    { code: 'pv_audience', libelle: 'PV audience' },
    { code: 'releve_general_service', libelle: 'Relevé général de service' },
    { code: 'indice', libelle: 'Indice' },
    { code: 'acte_carriere', libelle: 'Acte carrière' },
    { code: 'assignation', libelle: 'Assignation' },
    { code: 'convocation', libelle: 'Convocation' },
    { code: 'decision_justice', libelle: 'Décision de justice' },
  ]);
  readonly selectedDocFile = signal<File | null>(null);
  readonly DOC_LABELS = DOC_LABELS;
  readonly TYPE_ETAPES = TYPE_ETAPES;
  readonly TYPE_ETAPE_LABELS = TYPE_ETAPE_LABELS;
  readonly NATURE_DECISIONS = NATURE_DECISIONS;
  readonly NATURE_DECISION_LABELS = NATURE_DECISION_LABELS;
  readonly ISSUES = ISSUES;
  readonly ISSUE_LABELS = ISSUE_LABELS;
  readonly ROLES = ROLES;
  readonly ROLE_LABELS = ROLE_LABELS;
  readonly LIENS = LIENS;
  readonly LIEN_LABELS = LIEN_LABELS;

  tab = signal<TabKey>('infos');
  dossier = signal<Dossier | null>(null);
  editInfos = signal(false);
  showEtapeForm = signal(false);
  showAudForm = signal(false);
  showDocForm = signal(false);
  showImpForm = signal(false);
  showDJForm = signal(false);
  showDCForm = signal(false);

  editingEtapeId = signal<number | null>(null);
  editingAudId = signal<number | null>(null);
  editingDocId = signal<number | null>(null);
  editingImpKey = signal<{ numeroDossier: string; idPartie: number } | null>(null);
  editingDJKey = signal<{ numeroDossier: string; matricule: string } | null>(null);
  editingDCKey = signal<{ numeroDossier: string; identifiantCabinet: string } | null>(null);

  etapes = signal<EtapeDossier[]>([]);
  audiences = signal<AudienceDecision[]>([]);
  documents = signal<Document[]>([]);
  implications = signal<Implication[]>([]);
  dossierJuristes = signal<DossierJuriste[]>([]);
  dossierCabinets = signal<DossierCabinet[]>([]);

  parties = signal<Partie[]>([]);
  juristes = signal<Juriste[]>([]);
  cabinets = signal<Cabinet[]>([]);

  numeroDossier = '';

  infosForm!: FormGroup;
  etapeForm!: FormGroup;
  audForm!: FormGroup;
  docForm!: FormGroup;
  impForm!: FormGroup;
  djForm!: FormGroup;
  dcForm!: FormGroup;

  canMutateDossier() { return this.authService.canMutate(); }

  private onMutationError(action: string) {
    return (e: any) => alert(`Erreur ${action} : ` + (e?.error?.detail || e?.error?.message || e?.statusText || 'Erreur inconnue'));
  }
  canAssignJuriste() { return this.authService.canAssignJuriste(); }

  canDelete() {
    // La suppression d'un dossier purge désormais aussi ses éléments rattachés côté backend
    // (étapes, audiences, documents, implications, juristes, cabinets) dans une même transaction.
    return true;
  }

  deleteTooltip() {
    const parts = [];
    const e = this.etapes()?.length || 0;
    const a = this.audiences()?.length || 0;
    const d = this.documents()?.length || 0;
    const i = this.implications()?.length || 0;
    const j = this.dossierJuristes()?.length || 0;
    const c = this.dossierCabinets()?.length || 0;
    if (e) parts.push(`${e} étape(s)`);
    if (a) parts.push(`${a} audience(s)`);
    if (d) parts.push(`${d} document(s)`);
    if (i) parts.push(`${i} implication(s)`);
    if (j) parts.push(`${j} juriste(s) associés`);
    if (c) parts.push(`${c} cabinet(s) associés`);
    if (parts.length === 0) return 'Supprimer définitivement ce dossier';
    return 'Supprimer définitivement ce dossier, ainsi que : ' + parts.join(', ');
  }

  ngOnInit() {
    this.numeroDossier = this.route.snapshot.params['numeroDossier'];
    this.initForms();
    this.loadDossier();
    this.loadDocumentTypes();
    this.loadReferentiels();
  }

  private initForms() {
    this.infosForm = this.fb.group({
      numeroDossier: [''],
      dateOuverture: ['', Validators.required],
      nature: ['pension_retraite', Validators.required],
      montantReclame: [0],
      risqueFinancier: [0],
      fraisJustice: [0],
      resumeAffaire: [''],
      observation: [''],
    });
    this.etapeForm = this.fb.group({
      etape: ['ouvert' as EtapeDossierEtape, Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: [''],
    });
    this.audForm = this.fb.group({
      date: ['', Validators.required],
      lieuAudience: [''],
      etapeDossierId: ['', Validators.required],
      typeEtape: ['premiere_instance', Validators.required],
      natureDecision: [''],
      resumeDecision: [''],
      issuePourCarfo: [''],
      montantObtenu: [0],
      montantDu: [0],
      fraisJustice: [0],
    });
    this.docForm = this.fb.group({
      typeDocument: [''],
      dateAjout: [''],
      fichier: [''],
    });
    this.impForm = this.fb.group({
      idPartie: [null, Validators.required],
      role: ['demandeur', Validators.required],
      lienParente: [''],
    });
    this.djForm = this.fb.group({
      matricule: ['', Validators.required],
    });
    this.dcForm = this.fb.group({
      identifiantCabinet: ['', Validators.required],
      nomAvocatReferent: [''],
    });
  }

  private loadDocumentTypes() {
    this.docSvc.getDocumentTypes().subscribe({
      next: (types) => {
        const options = (types ?? []).map((entry) => {
          if (typeof entry === 'string') {
            return {
              code: entry,
              libelle: DOC_LABELS[entry as DocumentType] ?? entry,
            } as DocumentTypeOption;
          }
          return entry;
        });
        if (options.length > 0) {
          this.documentTypeOptions.set(options);
        }
      },
      error: () => {
        this.documentTypeOptions.set([
          { code: 'requete', libelle: 'Requête' },
          { code: 'piece_justificative', libelle: 'Pièce justificative' },
          { code: 'pv_audience', libelle: 'PV audience' },
          { code: 'releve_general_service', libelle: 'Relevé général de service' },
          { code: 'indice', libelle: 'Indice' },
          { code: 'acte_carriere', libelle: 'Acte carrière' },
          { code: 'assignation', libelle: 'Assignation' },
          { code: 'convocation', libelle: 'Convocation' },
          { code: 'decision_justice', libelle: 'Décision de justice' },
        ]);
      },
    });
  }

  private loadReferentiels() {
    this.partieSvc.getAll().subscribe((x) => this.parties.set(x));
    this.juristeSvc.getAll().subscribe((x) => this.juristes.set(x));
    this.cabinetSvc.getAll().subscribe((x) => this.cabinets.set(x));
  }

  typeLabel(code: string) {
    return this.documentTypeOptions().find((t) => t.code === code)?.libelle ?? code;
  }

  private loadDossier() {
    this.dossierSvc.getById(this.numeroDossier).subscribe((d) => {
      this.dossier.set(d);
      this.infosForm.patchValue({
        numeroDossier: d.numeroDossier,
        dateOuverture: d.dateOuverture,
        nature: d.typeContentieux?.nature || 'pension_retraite',
        montantReclame: d.montantReclame || 0,
        risqueFinancier: d.risqueFinancier || 0,
        fraisJustice: d.fraisJustice || 0,
        resumeAffaire: d.resumeAffaire || '',
        observation: d.observation || '',
      });
      this.loadRelations();
    });
  }

  private loadRelations() {
    this.etapeSvc.getByDossier(this.numeroDossier).subscribe((x) => this.etapes.set(x));
    this.audSvc.getByDossier(this.numeroDossier).subscribe((x) => this.audiences.set(x));
    this.docSvc.getByDossier(this.numeroDossier).subscribe((x) => this.documents.set(x));
    this.impSvc.getByDossier(this.numeroDossier).subscribe((x) => this.implications.set(x));
    this.djSvc.getByDossier(this.numeroDossier).subscribe((x) => this.dossierJuristes.set(x));
    this.dcSvc.getByDossier(this.numeroDossier).subscribe((x) => this.dossierCabinets.set(x));
  }

  supprimerDossier() {
    if (!confirm('Supprimer définitivement ce dossier ? Cette action est irréversible.')) return;
    this.dossierSvc.remove(this.numeroDossier).subscribe({
      next: () => {
        this.router.navigate(['/dossiers']);
      },
      error: (e: any) => {
        console.error('Erreur suppression dossier', e);
        alert('Erreur suppression : ' + (e?.error?.detail || e?.error?.message || e?.statusText || 'Erreur inconnue'));
      }
    });
  }

  saveInfos() {
    if (this.infosForm.invalid) return;
    const v = this.infosForm.getRawValue();
    const payload: any = {
      dateOuverture: v.dateOuverture,
      montantReclame: v.montantReclame,
      risqueFinancier: v.risqueFinancier,
      fraisJustice: v.fraisJustice,
      resumeAffaire: v.resumeAffaire,
      observation: v.observation,
      nature: v.nature,
    };
    this.dossierSvc.update(this.numeroDossier, payload).subscribe({
      next: (d) => {
        this.dossier.set(d);
        this.editInfos.set(false);
      },
      error: this.onMutationError('modification dossier'),
    });
  }

  toggleEtapeForm() {
    this.editingEtapeId.set(null);
    this.showEtapeForm.set(!this.showEtapeForm());
    if (this.showEtapeForm()) this.resetEtapeForm();
  }

  private resetEtapeForm() {
    this.etapeForm.reset({
      etape: 'ouvert', dateDebut: '', dateFin: ''
    });
  }

  editEtape(e: EtapeDossier) {
    this.showEtapeForm.set(false);
    this.editingEtapeId.set(e.id!);
    this.etapeForm.patchValue({
      etape: e.etape,
      dateDebut: e.dateDebut,
      dateFin: e.dateFin || '',
    });
  }

  cancelEtapeEdit() {
    this.editingEtapeId.set(null);
    this.resetEtapeForm();
  }

  saveEtape() {
    if (this.etapeForm.invalid) return;
    const v = this.etapeForm.getRawValue();
    this.etapeSvc.create({
      ...v,
      numeroDossier: this.numeroDossier,
      ...(v.dateFin === '' ? { dateFin: null } : {}),
    }).subscribe({
      next: (e) => {
        this.etapes.update((arr) => [...arr, e]);
        this.resetEtapeForm();
        this.showEtapeForm.set(false);
      },
      error: this.onMutationError('ajout étape'),
    });
  }

  updateEtape() {
    if (this.etapeForm.invalid || !this.editingEtapeId()) return;
    const v = this.etapeForm.getRawValue();
    const payload: Partial<EtapeDossier> = {
      etape: v.etape, dateDebut: v.dateDebut,
      dateFin: v.dateFin || null,
    };
    this.etapeSvc.update(this.editingEtapeId()!, payload).subscribe({
      next: (updated) => {
        this.etapes.update((arr) => arr.map((x) => x.id === updated.id ? updated : x));
        this.cancelEtapeEdit();
      },
      error: this.onMutationError('modification étape'),
    });
  }

  deleteEtape(id: number) {
    this.etapeSvc.delete(id).subscribe({
      next: () => this.etapes.update((arr) => arr.filter((e) => e.id !== id)),
      error: this.onMutationError('suppression étape'),
    });
  }

  toggleAudForm() {
    this.editingAudId.set(null);
    this.showAudForm.set(!this.showAudForm());
    if (this.showAudForm()) this.resetAudForm();
  }

  private resetAudForm() {
    this.audForm.reset({ typeEtape: 'premiere_instance' });
  }

  editAud(a: AudienceDecision) {
    this.showAudForm.set(false);
    this.editingAudId.set(a.numAudienceDecision!);
    this.audForm.patchValue({
      date: a.date,
      lieuAudience: a.lieuAudience || '',
      etapeDossierId: a.etapeDossierId,
      typeEtape: a.typeEtape,
      natureDecision: a.natureDecision || '',
      resumeDecision: a.resumeDecision || '',
      issuePourCarfo: a.issuePourCarfo || '',
      montantObtenu: a.montantObtenu || 0,
      montantDu: a.montantDu || 0,
      fraisJustice: a.fraisJustice || 0,
    });
  }

  cancelAudEdit() { this.editingAudId.set(null); this.resetAudForm(); }

  saveAud() {
    if (this.audForm.invalid) return;
    const v = this.audForm.getRawValue();
    const payload = { ...v, etapeDossierId: Number(v.etapeDossierId) };
    Object.keys(payload).forEach((k) => { if (payload[k] === '' || payload[k] === null) delete payload[k]; });
    this.audSvc.create(payload).subscribe({
      next: (a) => {
        this.audiences.update((arr) => [...arr, a]);
        this.resetAudForm();
        this.showAudForm.set(false);
      },
      error: this.onMutationError('ajout audience'),
    });
  }

  updateAud() {
    if (this.audForm.invalid || !this.editingAudId()) return;
    const v = this.audForm.getRawValue();
    const payload: any = { ...v, etapeDossierId: Number(v.etapeDossierId) };
    Object.keys(payload).forEach((k) => { if (payload[k] === '' || payload[k] === null) delete payload[k]; });
    this.audSvc.update(this.editingAudId()!, payload).subscribe({
      next: (updated) => {
        this.audiences.update((arr) => arr.map((x) => x.numAudienceDecision === updated.numAudienceDecision ? updated : x));
        this.cancelAudEdit();
      },
      error: this.onMutationError('modification audience'),
    });
  }

  deleteAud(id: number) {
    this.audSvc.delete(id).subscribe({
      next: () => this.audiences.update((arr) => arr.filter((a) => a.numAudienceDecision !== id)),
      error: this.onMutationError('suppression audience'),
    });
  }

  toggleDocForm() { this.editingDocId.set(null); this.showDocForm.set(!this.showDocForm()); if (this.showDocForm()) this.resetDocForm(); }
  private resetDocForm() {
    this.docForm.reset({ typeDocument: '', dateAjout: '' });
    this.selectedDocFile.set(null);
  }
  choisirDocument(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedDocFile.set(input.files?.[0] ?? null);
  }
  editDoc(d: Document) {
    this.showDocForm.set(false);
    this.editingDocId.set(d.idDocument!);
    this.docForm.patchValue({
      typeDocument: d.typeDocument || '',
      dateAjout: d.dateAjout || '',
      fichier: d.fichier || '',
    });
  }
  cancelDocEdit() { this.editingDocId.set(null); this.resetDocForm(); }
  saveDoc() {
    if (this.docForm.invalid) return;
    const v = this.docForm.getRawValue();

    if (this.selectedDocFile()) {
      const payload = new FormData();
      payload.append('fichier', this.selectedDocFile()!);
      payload.append('numeroDossier', this.numeroDossier);
      payload.append('typeDocument', v.typeDocument || '');
      if (v.dateAjout) payload.append('dateAjout', v.dateAjout);
      this.docSvc.upload(payload).subscribe({
        next: (d) => {
          this.documents.update((arr) => [...arr, d]);
          this.resetDocForm();
          this.showDocForm.set(false);
        },
        error: this.onMutationError('envoi document'),
      });
      return;
    }

    const payload = {
      numeroDossier: this.numeroDossier,
      ...(v.typeDocument ? { typeDocument: v.typeDocument } : {}),
      ...(v.dateAjout ? { dateAjout: v.dateAjout } : {}),
    };
    this.docSvc.create(payload).subscribe({
      next: (d) => {
        this.documents.update((arr) => [...arr, d]);
        this.resetDocForm();
        this.showDocForm.set(false);
      },
      error: this.onMutationError('ajout document'),
    });
  }
  updateDoc() {
    if (this.docForm.invalid || !this.editingDocId()) return;
    const v = this.docForm.getRawValue();
    this.docSvc.update(this.editingDocId()!, v).subscribe({
      next: (updated) => {
        this.documents.update((arr) => {
          const withoutOld = arr.filter((x) => x.idDocument !== updated.idDocument);
          return [...withoutOld, updated];
        });
        this.cancelDocEdit();
      },
      error: this.onMutationError('modification document'),
    });
  }
  deleteDoc(id: number) {
    this.docSvc.delete(id).subscribe({
      next: () => this.documents.update((arr) => arr.filter((d) => d.idDocument !== id)),
      error: this.onMutationError('suppression document'),
    });
  }

  toggleImpForm() { this.editingImpKey.set(null); this.showImpForm.set(!this.showImpForm()); if (this.showImpForm()) this.resetImpForm(); }
  private resetImpForm() { this.impForm.reset({ role: 'demandeur' }); }
  editImp(imp: Implication) {
    this.showImpForm.set(false);
    this.editingImpKey.set({ numeroDossier: imp.numeroDossier, idPartie: imp.idPartie });
    this.impForm.patchValue({ idPartie: imp.idPartie, role: imp.role, lienParente: imp.lienParente || '' });
  }
  cancelImpEdit() { this.editingImpKey.set(null); this.resetImpForm(); }
  saveImp() {
    if (this.impForm.invalid) return;
    const v = this.impForm.getRawValue();
    const payload = {
      numeroDossier: this.numeroDossier,
      idPartie: Number(v.idPartie),
      role: v.role,
      ...(v.lienParente && { lienParente: v.lienParente }),
    };
    this.impSvc.create(payload).subscribe({
      next: (i) => {
        this.implications.update((arr) => [...arr, i]);
        this.resetImpForm();
        this.showImpForm.set(false);
      },
      error: this.onMutationError('ajout implication'),
    });
  }
  updateImp() {
    if (this.impForm.invalid || !this.editingImpKey()) return;
    const v = this.impForm.getRawValue();
    const key = this.editingImpKey()!;
    const newPartieId = Number(v.idPartie);
    this.impSvc.update(key.numeroDossier, key.idPartie, {
      idPartie: newPartieId,
      role: v.role,
      ...(v.lienParente && { lienParente: v.lienParente }),
    }).subscribe({
      next: (updated) => {
        this.implications.update((arr) => {
          const withoutOld = arr.filter((x) => !(x.numeroDossier === key.numeroDossier && x.idPartie === key.idPartie));
          return [...withoutOld, updated];
        });
        this.cancelImpEdit();
      },
      error: this.onMutationError('modification implication'),
    });
  }
  deleteImp(imp: Implication) {
    this.impSvc.delete(imp.numeroDossier, imp.idPartie).subscribe({
      next: () => this.implications.update((arr) => arr.filter((i) => !(i.idPartie === imp.idPartie && i.numeroDossier === imp.numeroDossier))),
      error: this.onMutationError('suppression implication'),
    });
  }

  toggleDJForm() { this.editingDJKey.set(null); this.showDJForm.set(!this.showDJForm()); if (this.showDJForm()) this.resetDJForm(); }
  private resetDJForm() { this.djForm.reset(); }
  editDJ(dj: DossierJuriste) {
    this.showDJForm.set(false);
    this.editingDJKey.set({ numeroDossier: dj.numeroDossier, matricule: dj.matricule });
    this.djForm.patchValue({ matricule: dj.matricule });
  }
  cancelDJEdit() { this.editingDJKey.set(null); this.resetDJForm(); }
  saveDJ() {
    if (this.djForm.invalid) return;
    const v = this.djForm.getRawValue();
    this.djSvc.create({ numeroDossier: this.numeroDossier, matricule: v.matricule }).subscribe({
      next: (dj) => {
        this.dossierJuristes.update((arr) => [...arr, dj]);
        this.resetDJForm();
        this.showDJForm.set(false);
      },
      error: this.onMutationError('association juriste'),
    });
  }
  updateDJ() {
    if (this.djForm.invalid || !this.editingDJKey()) return;
    const v = this.djForm.getRawValue();
    const key = this.editingDJKey()!;
    this.djSvc.update(key.numeroDossier, key.matricule, { matricule: v.matricule }).subscribe({
      next: (updated) => {
        this.dossierJuristes.update((arr) => {
          const withoutOld = arr.filter((x) => !(x.numeroDossier === key.numeroDossier && x.matricule === key.matricule));
          return [...withoutOld, updated];
        });
        this.cancelDJEdit();
      },
      error: this.onMutationError('modification association juriste'),
    });
  }
  deleteDJ(dj: DossierJuriste) {
    this.djSvc.delete(dj.numeroDossier, dj.matricule).subscribe({
      next: () => this.dossierJuristes.update((arr) => arr.filter((x) => !(x.matricule === dj.matricule && x.numeroDossier === dj.numeroDossier))),
      error: this.onMutationError('suppression association juriste'),
    });
  }

  toggleDCForm() { this.editingDCKey.set(null); this.showDCForm.set(!this.showDCForm()); if (this.showDCForm()) this.resetDCForm(); }
  private resetDCForm() { this.dcForm.reset(); }
  editDC(dc: DossierCabinet) {
    this.showDCForm.set(false);
    this.editingDCKey.set({ numeroDossier: dc.numeroDossier, identifiantCabinet: dc.identifiantCabinet });
    this.dcForm.patchValue({
      identifiantCabinet: dc.identifiantCabinet,
      nomAvocatReferent: dc.nomAvocatReferent || '',
    });
  }
  cancelDCEdit() { this.editingDCKey.set(null); this.resetDCForm(); }
  saveDC() {
    if (this.dcForm.invalid) return;
    const v = this.dcForm.getRawValue();
    const payload = {
      numeroDossier: this.numeroDossier,
      identifiantCabinet: v.identifiantCabinet,
      ...(v.nomAvocatReferent && { nomAvocatReferent: v.nomAvocatReferent }),
    };
    this.dcSvc.create(payload).subscribe({
      next: (dc) => {
        this.dossierCabinets.update((arr) => [...arr, dc]);
        this.resetDCForm();
        this.showDCForm.set(false);
      },
      error: this.onMutationError('association cabinet'),
    });
  }
  updateDC() {
    if (this.dcForm.invalid || !this.editingDCKey()) return;
    const v = this.dcForm.getRawValue();
    const key = this.editingDCKey()!;
    this.dcSvc.update(key.numeroDossier, key.identifiantCabinet, {
      identifiantCabinet: v.identifiantCabinet,
      ...(v.nomAvocatReferent && { nomAvocatReferent: v.nomAvocatReferent }),
    }).subscribe({
      next: (updated) => {
        this.dossierCabinets.update((arr) => {
          const withoutOld = arr.filter((x) => !(x.numeroDossier === key.numeroDossier && x.identifiantCabinet === key.identifiantCabinet));
          return [...withoutOld, updated];
        });
        this.cancelDCEdit();
      },
      error: this.onMutationError('modification association cabinet'),
    });
  }
  deleteDC(dc: DossierCabinet) {
    this.dcSvc.delete(dc.numeroDossier, dc.identifiantCabinet).subscribe({
      next: () => this.dossierCabinets.update((arr) => arr.filter((x) => !(x.identifiantCabinet === dc.identifiantCabinet && x.numeroDossier === dc.numeroDossier))),
      error: this.onMutationError('suppression association cabinet'),
    });
  }

  partyName(idPartie: number): string {
    const p = this.parties().find((x) => x.id === idPartie);
    return p ? `${p.nom} ${p.prenom}` : `ID ${idPartie}`;
  }
  juristeInfo(matricule: string): Juriste | undefined { return this.juristes().find((j) => j.matricule === matricule); }
  cabinetInfo(id: string): Cabinet | undefined { return this.cabinets().find((c) => c.identifiantCabinet === id); }

  private downloadBlob(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }

  onExportPdf() {
    if (!this.dossier()) return;
    this.dossierSvc.exportPdf(this.dossier()!.numeroDossier).subscribe({
      next: (b: Blob) => {
        const filename = `dossier-${this.dossier()!.numeroDossier}-${new Date().toISOString().slice(0,10)}.pdf`;
        this.downloadBlob(b, filename);
      },
      error: (e) => console.error('Erreur export PDF dossier', e),
    });
  }

  onExportExcel() {
    if (!this.dossier()) return;
    this.dossierSvc.exportExcel(this.dossier()!.numeroDossier).subscribe({
      next: (b: Blob) => {
        const filename = `dossier-${this.dossier()!.numeroDossier}-${new Date().toISOString().slice(0,10)}.xlsx`;
        this.downloadBlob(b, filename);
      },
      error: (e) => console.error('Erreur export Excel dossier', e),
    });
  }

  onExportWord() {
    if (!this.dossier()) return;
    this.dossierSvc.exportWord(this.dossier()!.numeroDossier).subscribe({
      next: (b: Blob) => {
        const filename = `dossier-${this.dossier()!.numeroDossier}-${new Date().toISOString().slice(0,10)}.docx`;
        this.downloadBlob(b, filename);
      },
      error: (e) => console.error('Erreur export Word dossier', e),
    });
  }
}
