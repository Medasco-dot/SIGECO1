import { Component, inject, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { environment } from '../environments/environment';
import {
  DocumentService,
  Document,
  DocumentType,
  DocumentTypeOption,
} from './document.service';
import { DossierService, Dossier } from './dossier.service';

const DOCUMENT_TYPE_LABELS: Record<string, string> = {
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

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="page-intro">
      <div>
        <p class="eyebrow">Pièces jointes</p>
        <h1>Documents</h1>
        <p class="intro">Téléversez et consultez les pièces associées aux dossiers contentieux.</p>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Recherche</h2>
          <p class="muted">Filtrez les documents par type ou période d'ajout.</p>
        </div>
      </div>
      <div class="upload-grid">
        <label>Type de document
          <select [(ngModel)]="searchType" name="searchType">
            <option value="">Tous</option>
            @for(t of DOC_TYPES(); track t.code){
              <option [value]="t.code">{{ t.libelle }}</option>
            }
          </select>
        </label>
        <label>Date début (ajout)<input type="date" [(ngModel)]="searchDateDebut" name="searchDateDebut"/></label>
        <label>Date fin (ajout)<input type="date" [(ngModel)]="searchDateFin" name="searchDateFin"/></label>
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
          <h2>Ajouter une pièce</h2>
          <p class="muted">Étape 1 : choisissez le dossier, le type et le fichier à téléverser.</p>
        </div>
      </div>

      <div class="upload-grid">
        <label>Rechercher dossier<input [(ngModel)]="numeroRecherche" name="numeroRecherche" placeholder="Saisir un numéro ou fragment"/></label>
        <label style="align-self:end"><button type="button" (click)="rechercherDossiers()">Rechercher</button></label>
        <label>Choisir dossier<select [(ngModel)]="selectedNumeroDossier" name="selectedNumeroDossier">
          <option value="">-- sélectionner --</option>
          @for(d of dossierResults(); track d.numeroDossier){
            <option [value]="d.numeroDossier">{{ d.numeroDossier }} - {{ d.typeContentieux?.nature || '' }}</option>
          }
        </select></label>
        <label>Type<select [(ngModel)]="typeDocument" name="typeDocument">
          @for(t of DOC_TYPES(); track t.code){
            <option [value]="t.code">{{ t.libelle }}</option>
          }
        </select></label>
        <label class="file-picker">
          <span>{{ fichier ? fichier.name : 'Choisir un fichier' }}</span>
          <input type="file" (change)="choisir($event)" />
        </label>
      </div>

      <div class="form-actions">
        <button (click)="envoyer()" [disabled]="!fichier || !selectedNumeroDossier">Téléverser la pièce</button>
      </div>

      @if(message()){
        <p class="notice">{{message()}}</p>
      }
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Ajouter un type de document</h2>
          <p class="muted">Étape 2 : créez un type qui sera ensuite proposé dans la liste des documents existants.</p>
        </div>
      </div>

      <div class="upload-grid">
        <label>Code du type<input [(ngModel)]="newTypeCode" name="newTypeCode" placeholder="ex: certificat_medical"/></label>
        <label>Libellé<input [(ngModel)]="newTypeLibelle" name="newTypeLibelle" placeholder="Certificat médical"/></label>
      </div>

      <div class="form-actions">
        <button type="button" class="button secondary" (click)="creerTypeDocument()">Ajouter type à la liste</button>
      </div>

      @if(typeMessage()){
        <p class="notice">{{typeMessage()}}</p>
      }

      <div class="table-shell">
        <table>
          <thead><tr><th>Code</th><th>Libellé</th><th></th></tr></thead>
          <tbody>
            @for(t of DOC_TYPES(); track t.code){
              <tr>
                <td>{{t.code}}</td>
                <td>{{t.libelle}}</td>
                <td><button class="link danger" (click)="supprimerTypeDocument(t.code)">Supprimer</button></td>
              </tr>
            }
            @empty {
              <tr><td colspan="3" class="empty">Aucun type de document.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Documents enregistrés</h2>
          <p class="muted">Historique des pièces enregistrées dans la plateforme.</p>
        </div>
      </div>
      <div class="table-shell">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>N° Dossier</th>
              <th>Type</th>
              <th>Date ajout</th>
              <th>Fichier</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for(d of documents();track d.idDocument){
              <tr>
                <td>{{d.idDocument}}</td>
                <td>{{d.numeroDossier || '-'}}</td>
                <td>{{d.typeDocument ? typeLabel(d.typeDocument) : '-'}}</td>
                <td>{{d.dateAjout || '-'}}</td>
                <td>{{d.fichier || '-'}}</td>
                <td>
                  <button class="link" (click)="charger(d)">Modifier</button>
                  <button class="link danger" (click)="supprimer(d.idDocument!)">Supprimer</button>
                </td>
              </tr>
            }
            @empty{
              <tr><td colspan="6" class="empty">Aucun document.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </section>
  `,
  styles: [
    `
      .upload-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 16px;
        margin-bottom: 14px;
      }

      .file-picker {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 14px 16px;
        border: 1px dashed var(--carfo-line);
        border-radius: 12px;
        background: var(--carfo-paper);
        color: var(--carfo-muted);
        cursor: pointer;
        margin-bottom: 14px;
      }

      .file-picker input {
        display: none;
      }

      .form-actions {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
      }

      @media (max-width: 900px) {
        .upload-grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class DocumentsComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly docSvc = inject(DocumentService);
  private readonly api = `${environment.apiUrl}/api/documents`;

  readonly DOC_TYPES = signal<DocumentTypeOption[]>([]);
  newTypeCode = '';
  newTypeLibelle = '';
  typeMessage = signal('');

  // dossier selection
  numeroDossier = '';
  numeroRecherche = '';
  dossierResults = signal<Dossier[]>([]);
  selectedNumeroDossier = '';

  typeDocument: DocumentType = 'requete';
  fichier?: File;
  message = signal('');
  documents = signal<Document[]>([]);
  selectedId?: number | null = null;

  searchType: DocumentType | '' = '';
  searchDateDebut = '';
  searchDateFin = '';
  searchMessage = signal('');

  private readonly dossierSvc = inject(DossierService);

  ngOnInit() {
    this.chargerTypes();
    this.chargerTous();
  }

  private chargerTypes() {
    this.docSvc.getDocumentTypes().subscribe({
      next: (types) => {
        const options = (types ?? []).map((entry) => {
          if (typeof entry === 'string') {
            return {
              code: entry,
              libelle: DOCUMENT_TYPE_LABELS[entry] ?? entry,
            } as DocumentTypeOption;
          }
          return entry;
        });
        this.DOC_TYPES.set(options);
      },
      error: () => this.message.set('Impossible de charger les types de documents.'),
    });
  }

  typeLabel(code: string) {
    return this.DOC_TYPES().find((t) => t.code === code)?.libelle ?? code;
  }

  private chargerTous() {
    this.docSvc.getAll().subscribe({
      next: (list) => this.documents.set(list),
      error: () => this.message.set('Impossible de charger les documents.'),
    });
  }

  rechercher() {
    this.searchMessage.set('');
    if (this.searchType) {
      this.docSvc.rechercherParType(this.searchType).subscribe({
        next: (list) => this.documents.set(list),
        error: () => this.searchMessage.set('Erreur lors de la recherche par type.'),
      });
    } else if (this.searchDateDebut && this.searchDateFin) {
      this.docSvc.rechercherParDate(this.searchDateDebut, this.searchDateFin).subscribe({
        next: (list) => this.documents.set(list),
        error: () => this.searchMessage.set('Erreur lors de la recherche par date.'),
      });
    } else {
      this.searchMessage.set('Veuillez saisir au moins un critère de recherche.');
    }
  }

  rechercherDossiers() {
    if (!this.numeroRecherche || !this.numeroRecherche.trim()) {
      this.message.set('Saisissez un numéro ou un fragment pour rechercher.');
      return;
    }
    this.dossierSvc.rechercherParNumero(this.numeroRecherche.trim()).subscribe({
      next: (list) => {
        this.dossierResults.set(list);
        if (!list || list.length === 0) {
          this.message.set('Aucun dossier trouvé.');
        } else {
          this.message.set(list.length + ' dossier(s) trouvé(s).');
        }
      },
      error: () => this.message.set("Erreur lors de la recherche de dossiers."),
    });
  }

  charger(d: Document) {
    this.selectedId = d.idDocument || null;
    this.selectedNumeroDossier = d.numeroDossier || '';
    this.typeDocument = (d.typeDocument as DocumentType) || 'requete';
    this.message.set(`Édition du document ${d.idDocument}`);
  }

  supprimer(id?: number) {
    if (!id) return;
    if (!confirm('Supprimer ce document ?')) return;
    this.docSvc.delete(id).subscribe({
      next: () => {
        this.chargerTous();
        this.message.set('Document supprimé.');
      },
      error: (err) => {
        console.error('Erreur suppression document', err);
        this.message.set('Impossible de supprimer le document.');
      }
    });
  }

  resetRecherche() {
    this.searchType = '';
    this.searchDateDebut = '';
    this.searchDateFin = '';
    this.searchMessage.set('');
    this.chargerTous();
  }

  choisir(e: Event) {
    this.fichier = (e.target as HTMLInputElement).files?.[0];
  }

  creerTypeDocument() {
    this.typeMessage.set('');
    if (!this.newTypeCode.trim() || !this.newTypeLibelle.trim()) {
      this.typeMessage.set('Saisissez un code et un libellé pour créer un type de document.');
      return;
    }
    this.docSvc.createDocumentType({ code: this.newTypeCode.trim(), libelle: this.newTypeLibelle.trim() }).subscribe({
      next: () => {
        this.newTypeCode = '';
        this.newTypeLibelle = '';
        this.chargerTypes();
        this.typeMessage.set('Type de document ajouté à la liste.');
      },
      error: (e) => this.typeMessage.set(e?.error?.detail || "Impossible de créer ce type de document."),
    });
  }

  supprimerTypeDocument(code: string) {
    if (!confirm(`Supprimer le type de document "${code}" ?`)) return;
    this.typeMessage.set('');
    this.docSvc.deleteDocumentType(code).subscribe({
      next: () => {
        this.chargerTypes();
        this.typeMessage.set('Type de document supprimé.');
      },
      error: (e) => this.typeMessage.set(e?.error?.detail || "Impossible de supprimer ce type de document."),
    });
  }

  envoyer() {
    // If editing an existing doc and no new file chosen, update metadata via PUT
    if (this.selectedId && !this.fichier) {
      const payload: Partial<Document> = { numeroDossier: this.selectedNumeroDossier, typeDocument: this.typeDocument };
      this.docSvc.update(this.selectedId, payload).subscribe({
        next: () => {
          this.message.set('Document mis à jour.');
          this.selectedId = null;
          this.selectedNumeroDossier = '';
          this.fichier = undefined;
          this.chargerTous();
        },
        error: () => this.message.set("La mise à jour a échoué."),
      });
      return;
    }

    // Upload new file (create)
    if (!this.fichier || !this.selectedNumeroDossier) return;
    const data = new FormData();
    data.append('fichier', this.fichier);
    data.append('numeroDossier', this.selectedNumeroDossier);
    data.append('typeDocument', this.typeDocument);
    // If editing and file provided, call upload endpoint (assumed to create a new record or replace)
    this.http.post(`${this.api}/upload`, data).subscribe({
      next: () => {
        this.message.set(this.selectedId ? 'Document mis à jour.' : 'Document enregistré.');
        this.selectedId = null;
        this.selectedNumeroDossier = '';
        this.fichier = undefined;
        this.chargerTous();
      },
      error: () => this.message.set("L'envoi a échoué. Vérifiez le numéro de dossier."),
    });
  }
}
