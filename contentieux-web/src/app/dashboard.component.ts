import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import {
  StatistiquesService,
  StatistiquesSynthese,
  DossierLeger,
} from './statistiques.service';
import { ETAPE_LABELS } from './dossier.service';
import { AuthService } from './auth.service';

@Component({
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  template: `
    <section class="dashboard-hero">
      <div class="hero-copy">
        <p class="eyebrow">Portail digital Comfort CARFO · Service contentieux</p>
        <h1>Tableau de bord de supervision</h1>
        <p class="intro">
          Vue consolidée du risque financier et de l’état d’avancement des contentieux.
          Conforme au suivi que le chef de service souhaitait remplacer l’Excel existant.
        </p>
        <div class="hero-actions">
          @if (canCreateDossier()) {
            <a class="button" routerLink="/dossiers/nouveau">+ Nouveau dossier</a>
          }
          <a class="button" routerLink="/dossiers">Voir tous les dossiers</a>

          @if (canViewStatistiques()) {
            <!-- Export buttons for statistics -->
            <button class="button" (click)="onExportPdf()">Exporter en PDF</button>
            <button class="button" (click)="onExportExcel()">Exporter en Excel</button>
            <button class="button" (click)="onExportWord()">Exporter en Word</button>
          }
        </div>
      </div>
      @if (canViewStatistiques()) {
        <div class="hero-highlight">
          <span class="hero-pill">Données en temps réel</span>
          <h3>Risque financier cumulé</h3>
          <p class="risk">
            {{ s()?.risqueFinancierCumule ?? 0 | number:'1.2-2' }} FCFA
          </p>
        </div>
      }
    </section>

    @if (canViewStatistiques()) {
    <div class="metrics">
      <article class="stat-card kpi-primary">
        <small>Dossiers au total</small>
        <strong>{{ s()?.totalDossiers ?? 0 }}</strong>
      </article>
      <article class="stat-card kpi-orange">
        <small>En cours d’exécution (Ouvert)</small>
        <strong>{{ s()?.dossiersOuverts ?? 0 }}</strong>
      </article>
      <article class="stat-card kpi-blue">
        <small>En appel</small>
        <strong>{{ s()?.dossiersEnAppel ?? 0 }}</strong>
      </article>
      <article class="stat-card kpi-red">
        <small>En cassation</small>
        <strong>{{ s()?.dossiersEnCassation ?? 0 }}</strong>
      </article>
      <article class="stat-card kpi-red-alt">
        <small>Alertes urgentes</small>
        <strong>{{ s()?.dossiersAlerte ?? 0 }}</strong>
      </article>
    </div>

    <div class="metrics small">
      <article class="stat-card mini">
        <small>Frais de justice cumulés</small>
        <strong>{{ s()?.fraisJusticeCumules ?? 0 | number:'1.2-2' }} FCFA</strong>
      </article>
      <article class="stat-card mini">
        <small>Montant réclamé cumulé</small>
        <strong>{{ s()?.montantReclameCumule ?? 0 | number:'1.2-2' }} FCFA</strong>
      </article>
    </div>

    <section class="grid-2">
      <div class="panel">
        <div class="panel-head">
          <h2>Répartition par type de contentieux</h2>
        </div>
        @if (typeEntries().length > 0) {
          <div class="breakdown">
            @for (entry of typeEntries(); track entry[0]) {
              <div class="breakdown-row">
                <span>{{ entry[0] }}</span>
                <strong>{{ entry[1] }}</strong>
              </div>
            }
          </div>
        } @else {
          <p class="empty">Aucune donnée disponible.</p>
        }
      </div>

      <div class="panel">
        <div class="panel-head">
          <h2>Répartition par étape actuelle du dossier</h2>
        </div>
        @if (etapeEntries().length > 0) {
          <div class="breakdown">
            @for (entry of etapeEntries(); track entry[0]) {
              <div class="breakdown-row">
                <span>{{ etapeLabel(entry[0]) }}</span>
                <strong>{{ entry[1] }}</strong>
              </div>
            }
          </div>
        } @else {
          <p class="empty">Aucune donnée disponible.</p>
        }
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Top 5 — Dossiers les plus risqués</h2>
          <p>Classés par risque financier décroissant.</p>
        </div>
      </div>
      @if (s()?.top5Risques && s()!.top5Risques!.length > 0) {
        <table class="table striped">
          <thead>
            <tr>
              <th>N° Dossier</th>
              <th>Type</th>
              <th>Étape</th>
              <th class="right">Risque</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (d of s()!.top5Risques!; track d.numeroDossier) {
              <tr>
                <td>{{ d.numeroDossier }}</td>
                <td>{{ d.typeContentieux || '—' }}</td>
                <td>{{ etapeLabel(d.etapeCourante || '') }}</td>
                <td class="right">{{ d.risqueFinancier ?? 0 | number:'1.2-2' }} FCFA</td>
                <td class="right">
                  <a [routerLink]="['/dossiers', d.numeroDossier]" class="link">Ouvrir</a>
                </td>
              </tr>
            }
          </tbody>
        </table>
      } @else {
        <p class="empty">Aucun dossier avec risque financier renseigné.</p>
      }
    </section>
    } @else {
      <section class="panel">
        <div class="panel-head">
          <h2>Bienvenue</h2>
        </div>
        <p>Les statistiques consolidées sont réservées au chef de service et à la Direction Générale. Utilisez le menu pour créer ou consulter vos dossiers.</p>
      </section>
    }
  `,
  styles: [
    `
      .dashboard-hero {
        display: flex;
        justify-content: space-between;
        align-items: stretch;
        gap: 20px;
        margin-bottom: 18px;
        padding: 24px;
        border-radius: 20px;
        background: linear-gradient(135deg, var(--carfo-blue-dark), var(--carfo-blue));
        color: white;
        box-shadow: 0 20px 45px rgba(0, 59, 99, 0.14);
      }
      .hero-copy { flex: 1; }
      .eyebrow { color: #9fd6c2; margin: 0 0 8px; text-transform: uppercase; letter-spacing: .08em; font-size: 11px; font-weight: 700; }
      .hero-copy h1 { margin: 0 0 10px; font-size: clamp(24px, 3vw, 34px); }
      .intro { color: #dceff4; max-width: 620px; line-height: 1.7; }
      .hero-actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 18px; }
      .button.secondary { background: rgba(255,255,255,0.14); color: white; border: 1px solid rgba(255,255,255,0.2); }
      .hero-highlight {
        min-width: 280px; max-width: 360px;
        background: rgba(255,255,255,0.12);
        border: 1px solid rgba(255,255,255,0.16);
        border-radius: 16px; padding: 18px; backdrop-filter: blur(8px);
        display: flex; flex-direction: column; justify-content: space-between;
      }
      .hero-pill {
        display: inline-block; background: rgba(17,135,93,0.24); color: #cfeede;
        border-radius: 999px; padding: 5px 10px; font-size: 11px; font-weight: 700;
        text-transform: uppercase; letter-spacing: 0.08em; align-self: flex-start;
      }
      .hero-highlight h3 { margin: 10px 0 8px; font-size: 18px; }
      .hero-highlight .risk { font-size: 30px; font-weight: 800; margin: 6px 0 12px; color: #ffd7a6; }
      .hero-highlight small { color: #e0f2f5; }
      .metrics {
        display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 14px;
      }
      .metrics.small { grid-template-columns: repeat(2, 1fr); }
      @media (max-width: 1100px) { .metrics { grid-template-columns: repeat(2, 1fr); } }
      @media (max-width: 560px)  { .metrics, .metrics.small { grid-template-columns: 1fr; } }
      .stat-card {
        background: white; border-radius: 14px; padding: 16px 18px;
        border: 1px solid #e7ecef; box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
      }
      .stat-card small { color: #5a6b7a; font-size: 12px; display:block; margin-bottom: 6px; }
      .stat-card strong { font-size: 22px; color: #10202e; font-weight: 700; }
      .stat-card.mini strong { font-size: 17px; }
      .kpi-primary { border-left: 6px solid var(--carfo-blue); }
      .kpi-orange  { border-left: 6px solid #f59e0b; }
      .kpi-blue    { border-left: 6px solid #3b82f6; }
      .kpi-red     { border-left: 6px solid #ef4444; }
      .kpi-red-alt { border-left: 6px solid #b91c1c; background: #fff7f7; }
      .grid-2 {
        display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;
      }
      @media (max-width: 900px) { .grid-2 { grid-template-columns: 1fr; } .dashboard-hero { flex-direction: column; } .hero-highlight { max-width: none; min-width: 0; } }
      .panel {
        background: white; border-radius: 16px; padding: 20px 22px;
        border: 1px solid #e7ecef; margin-bottom: 16px;
      }
      .panel-head h2 { margin: 0 0 4px; font-size: 17px; color: var(--carfo-green-dark); }
      .panel-head p { margin: 0; color: #5a6b7a; font-size: 13px; }
      .panel-head { display:flex; align-items: flex-start; justify-content: space-between; margin-bottom: 14px; }
      .breakdown { display: grid; gap: 10px; }
      .table { width: 100%; border-collapse: collapse; font-size: 14px; }
      .table th, .table td { padding: 12px 14px; border-bottom: 1px solid #e3ece7; }
      .table th { background: var(--carfo-green); color: white; text-align: left; font-size: 11px; text-transform: uppercase; letter-spacing: 0.06em; }
      .table.striped tbody tr:nth-child(even) { background: #f5fbf8; }
      .table td { color: #10202e; }
      .breakdown-row {
        display: flex; justify-content: space-between; align-items: center; padding: 12px 16px;
        background: var(--carfo-green-light); border-radius: 8px; font-size: 14px;
      }
      .breakdown-row strong { color: var(--carfo-green-dark); font-size: 15px; }
      .empty { color: #7a8c9a; font-style: italic; padding: 8px 4px; }
      .link { color: var(--carfo-blue); text-decoration: none; font-weight: 600; }
      .link:hover { text-decoration: underline; }
    `
  ],
})
export class DashboardComponent {
  private statsApi = inject(StatistiquesService);
  private readonly authService = inject(AuthService);

  s = signal<StatistiquesSynthese | null>(null);
  typeEntries = signal<[string, number][]>([]);
  etapeEntries = signal<[string, number][]>([]);

  canCreateDossier() {
    return this.authService.canMutate();
  }

  canViewStatistiques() {
    return this.authService.canViewStatistiques();
  }

  etapeLabel(k: string) {
    return (ETAPE_LABELS as Record<string, string>)[k] ?? k;
  }

  constructor() {
    if (!this.canViewStatistiques()) {
      return;
    }
    this.statsApi.synthese().subscribe({
      next: (res) => {
        this.s.set(res);
        this.typeEntries.set(Object.entries(res.repartitionParType ?? {}));
        this.etapeEntries.set(Object.entries(res.repartitionParEtape ?? {}));
      },
      error: (e) => console.error('Erreur chargement stats', e),
    });
  }

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
    this.statsApi.exportPdf().subscribe({
      next: (b: Blob) => {
        const filename = `statistiques-contentieux-${new Date().toISOString().slice(0,10)}.pdf`;
        this.downloadBlob(b, filename);
      },
      error: (e) => console.error('Erreur export PDF', e),
    });
  }

  onExportExcel() {
    this.statsApi.exportExcel().subscribe({
      next: (b: Blob) => {
        const filename = `statistiques-contentieux-${new Date().toISOString().slice(0,10)}.xlsx`;
        this.downloadBlob(b, filename);
      },
      error: (e) => console.error('Erreur export Excel', e),
    });
  }

  onExportWord() {
    this.statsApi.exportWord().subscribe({
      next: (b: Blob) => {
        const filename = `statistiques-contentieux-${new Date().toISOString().slice(0,10)}.docx`;
        this.downloadBlob(b, filename);
      },
      error: (e) => console.error('Erreur export Word', e),
    });
  }
}

