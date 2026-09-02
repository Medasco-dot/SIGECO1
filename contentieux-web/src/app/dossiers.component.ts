import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import {
  Dossier, DossierService, DossierSearchCriteria,
  TypeContentieuxNature, EtapeDossierEtape,
  NATURE_LABELS, ETAPES_DOSSIER, ETAPE_LABELS,
} from './dossier.service';
import { Juriste, JuristeService } from './juriste.service';
import { Cabinet, CabinetService } from './cabinet.service';
import { AuthService } from './auth.service';

const NATURES: TypeContentieuxNature[] = ['pension_retraite', 'pension_reversement', 'acte_carriere', 'marche_public', 'penal', 'autre'];

@Component({
  standalone: true,
  imports: [RouterLink, DecimalPipe, ReactiveFormsModule],
  template: `
    <section class="page-intro">
      <div>
        <h1>Dossiers contentieux</h1>
        <p class="intro">Recherchez et filtrez les dossiers avec une vue claire sur l’état du contentieux.</p>
      </div>
      <div class="action-stack">
        @if (canCreateDossier()) {
          <a class="button" routerLink="/dossiers/nouveau">+ Nouveau dossier</a>
        }
      </div>
    </section>

    <section class="summary-strip">
      <article class="summary-card">
        <span class="summary-label">Résultats</span>
        <strong>{{ totalElements() }}</strong>
      </article>
      <article class="summary-card">
        <span class="summary-label">Taille de page</span>
        <strong>{{ size() }}</strong>
      </article>
      <article class="summary-card">
        <span class="summary-label">Page courante</span>
        <strong>{{ page() + 1 }}</strong>
      </article>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>Recherche multicritère</h2>
        <button class="link" (click)="resetSearch()">Réinitialiser</button>
      </div>
      <form class="form" [formGroup]="searchForm" (ngSubmit)="rechercher()">
        <div class="grid-3">
          <label>Numéro dossier<input formControlName="numeroDossier" placeholder="ex: DOS-2025-0001"/></label>
          <label>Nature du contentieux
            <select formControlName="nature">
              <option value="">Toutes</option>
              @for(n of NATURES; track n){<option [value]="n">{{ NATURE_LABELS[n] }}</option>}
            </select>
          </label>
          <label>Étape actuelle
            <select formControlName="etapeActuelle">
              <option value="">Toutes</option>
              @for(e of ETAPES_DOSSIER; track e){<option [value]="e">{{ ETAPE_LABELS[e] }}</option>}
            </select>
          </label>
        </div>
        <div class="grid-3">
          <label>Risque min (FCFA)<input type="number" formControlName="risqueFinancierMin" min="0"/></label>
          <label>Date d'ouverture (min)<input type="date" formControlName="dateOuvertureMin"/></label>
          <label>Date d'ouverture (max)<input type="date" formControlName="dateOuvertureMax"/></label>
        </div>
        <div class="grid-3">
          <label>Partie (nom/prénom)<input formControlName="partieNom"/></label>
          <label>Juriste
            <select formControlName="matriculeJuriste">
              <option value="">Tous</option>
              @for(j of juristes(); track j.matricule){
                <option [value]="j.matricule">{{ j.matricule }} — {{ j.nom }} {{ j.prenoms }}</option>
              }
            </select>
          </label>
          <label>Cabinet
            <select formControlName="identifiantCabinet">
              <option value="">Tous</option>
              @for(c of cabinets(); track c.identifiantCabinet){
                <option [value]="c.identifiantCabinet">{{ c.identifiantCabinet }} — {{ c.nomCabinet }}</option>
              }
            </select>
          </label>
        </div>
        <div class="grid-3">
          <label>Taille de page
            <select formControlName="size">
              <option [value]="5">5</option>
              <option [value]="10">10</option>
              <option [value]="25">25</option>
              <option [value]="50">50</option>
            </select>
          </label>
        </div>
        <div class="row gap">
          <button class="primary" type="submit">Rechercher</button>
          <button class="button" type="button" (click)="resetSearch()">Réinitialiser</button>
        </div>
      </form>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Résultats</h2>
          <p class="muted">
            {{ totalElements() }} dossier{{ totalElements() > 1 ? 's' : '' }} · page {{ page() + 1 }} / {{ totalPages() || 1 }}
          </p>
        </div>
      </div>
      <div class="table-shell">
        <table>
          <thead>
            <tr>
              <th>Numéro</th>
              <th>Ouverture</th>
              <th>Nature</th>
              <th class="num">Risque</th>
              <th class="num">Réclamé</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for(d of items(); track d.numeroDossier){
              <tr>
                <td><strong>{{ d.numeroDossier }}</strong></td>
                <td>{{ d.dateOuverture }}</td>
                <td>{{ d.typeContentieux?.nature ? NATURE_LABELS[d.typeContentieux!.nature] : '—' }}</td>
                <td class="num">{{ (d.risqueFinancier || 0) | number }}</td>
                <td class="num">{{ (d.montantReclame || 0) | number }}</td>
                <td><a class="link" [routerLink]="['/dossiers', d.numeroDossier]">Ouvrir</a></td>
              </tr>
            } @empty {
              <tr><td colspan="6" class="empty">Aucun dossier ne correspond aux critères.</td></tr>
            }
          </tbody>
        </table>
      </div>

      <nav class="pagination" aria-label="Pagination">
        <button class="button" (click)="goTo(0)" [disabled]="page() === 0">« Début</button>
        <button class="button" (click)="goTo(page() - 1)" [disabled]="page() === 0">Précédent</button>
        <span class="muted">Page {{ page() + 1 }} / {{ totalPages() || 1 }}</span>
        <button class="button" (click)="goTo(page() + 1)" [disabled]="page() + 1 >= totalPages()">Suivant</button>
        <button class="button" (click)="goTo(totalPages() - 1)" [disabled]="page() + 1 >= totalPages()">Fin »</button>
      </nav>
    </section>
  `,
  styles: [
    `
      .page-intro {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 16px;
        margin-bottom: 4px;
      }
      .action-stack {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
      }
      .summary-strip {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 14px;
        margin-bottom: 8px;
      }
      .summary-card {
        background: white;
        border: 1px solid var(--carfo-line);
        border-radius: 6px;
        padding: 14px 16px;
      }
      .summary-label {
        display: block;
        color: var(--carfo-muted);
        font-size: 12px;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        margin-bottom: 8px;
      }
      .summary-card strong {
        font-size: 24px;
        color: var(--carfo-blue);
      }
      .table-shell {
        overflow-x: auto;
        border: 1px solid var(--carfo-line);
        border-radius: 12px;
      }
      .pagination {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
        margin-top: 16px;
      }
      @media (max-width: 900px) {
        .page-intro {
          flex-direction: column;
        }
        .summary-strip {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class DossiersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dossierSvc = inject(DossierService);
  private readonly juristeSvc = inject(JuristeService);
  private readonly cabinetSvc = inject(CabinetService);
  private readonly authService = inject(AuthService);

  readonly NATURES = NATURES;
  readonly NATURE_LABELS = NATURE_LABELS;
  readonly ETAPES_DOSSIER: EtapeDossierEtape[] = ETAPES_DOSSIER;
  readonly ETAPE_LABELS = ETAPE_LABELS;

  searchForm!: FormGroup;
  items = signal<Dossier[]>([]);
  page = signal(0);
  size = signal(10);
  totalElements = signal(0);
  totalPages = computed(() => Math.ceil(this.totalElements() / this.size() || 1));
  juristes = signal<Juriste[]>([]);
  cabinets = signal<Cabinet[]>([]);

  canCreateDossier() {
    return this.authService.canMutate();
  }

  ngOnInit() {
    this.searchForm = this.fb.group({
      numeroDossier: [''],
      nature: [''],
      etapeActuelle: [''],
      dateOuvertureMin: [''],
      dateOuvertureMax: [''],
      risqueFinancierMin: [null],
      partieNom: [''],
      matriculeJuriste: [''],
      identifiantCabinet: [''],
      size: [10, Validators.required],
    });
    this.juristeSvc.getAll().subscribe((x) => this.juristes.set(x));
    this.cabinetSvc.getAll().subscribe((x) => this.cabinets.set(x));
    this.size.set(10);
    this.rechercher();
  }

  buildCriteria(): DossierSearchCriteria {
    const v = this.searchForm.getRawValue();
    const criteria: DossierSearchCriteria = {};
    if (v['numeroDossier']?.trim()) criteria.numeroDossier = v['numeroDossier'].trim();
    if (v['nature']) criteria.typeContentieux = v['nature'];
    if (v['etapeActuelle']) criteria.etapeCourante = v['etapeActuelle'];
    if (v['dateOuvertureMin']) criteria.dateOuvertureMin = v['dateOuvertureMin'];
    if (v['dateOuvertureMax']) criteria.dateOuvertureMax = v['dateOuvertureMax'];
    if (v['risqueFinancierMin'] != null && v['risqueFinancierMin'] !== '') {
      criteria.risqueFinancierMin = Number(v['risqueFinancierMin']);
    }
    if (v['partieNom']?.trim()) criteria.nomPartie = v['partieNom'].trim();
    if (v['matriculeJuriste']) criteria.matriculeJuriste = v['matriculeJuriste'];
    if (v['identifiantCabinet']) criteria.identifiantCabinet = v['identifiantCabinet'];
    return criteria;
  }

  rechercher() {
    const criteria = this.buildCriteria();
    const pageSize = Number(this.searchForm.get('size')?.value || 10);
    this.size.set(pageSize);
    this.page.set(0);
    this.dossierSvc.search(criteria, 0, pageSize).subscribe({
      next: (res: any) => {
        this.items.set(res.content || []);
        this.totalElements.set(res.totalElements || 0);
        this.page.set(res.number || 0);
        this.size.set(res.size || pageSize);
      },
      error: () => alert('La recherche a échoué. Veuillez réessayer.'),
    });
  }

  goTo(p: number) {
    if (p < 0 || p >= this.totalPages()) return;
    const criteria = this.buildCriteria();
    this.dossierSvc.search(criteria, p, this.size()).subscribe({
      next: (res: any) => {
        this.items.set(res.content || []);
        this.totalElements.set(res.totalElements || 0);
        this.page.set(res.number || p);
        this.size.set(res.size || this.size());
      },
      error: () => alert('Le changement de page a échoué. Veuillez réessayer.'),
    });
  }

  resetSearch() {
    this.searchForm.reset({ size: 10 });
    this.rechercher();
  }
}
