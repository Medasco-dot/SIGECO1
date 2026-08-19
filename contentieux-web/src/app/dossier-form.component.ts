import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DossierService, TypeContentieuxNature, NATURE_LABELS } from './dossier.service';
import { AuthService } from './auth.service';
import { finalize } from 'rxjs/operators';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="page-intro compact">
      <div>
        <p class="eyebrow">Dossiers</p>
        <h1>Créer un nouveau dossier</h1>
        <p class="intro">Enregistrez rapidement un nouveau dossier contentieux avec les informations essentielles.</p>
      </div>
      <a class="button secondary" routerLink="/dossiers">Retour à la liste</a>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Informations du dossier</h2>
          <p class="muted">Les champs marqués d'un astérisque sont obligatoires.</p>
        </div>
      </div>

      <form class="form" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form-grid">
          <label>Date d'ouverture *<input type="date" formControlName="dateOuverture"/></label>
          <label>Type de contentieux *<select formControlName="nature">
            <option value="pension_retraite">{{ NATURE_LABELS.pension_retraite }}</option>
            <option value="pension_reversement">{{ NATURE_LABELS.pension_reversement }}</option>
            <option value="acte_carriere">{{ NATURE_LABELS.acte_carriere }}</option>
            <option value="marche_public">{{ NATURE_LABELS.marche_public }}</option>
            <option value="penal">{{ NATURE_LABELS.penal }}</option>
            <option value="autre">{{ NATURE_LABELS.autre }}</option>
          </select></label>
          <label>Montant réclamé<input type="number" formControlName="montantReclame" min="0"/></label>
        </div>

        <div class="form-actions">
          <button type="submit" [disabled]="form.invalid || isSubmitting()">Enregistrer</button>
          <a class="button secondary" routerLink="/dossiers">Annuler</a>
        </div>
      </form>
    </section>
  `,
  styles: [
    `
      .page-intro.compact {
        margin-bottom: 4px;
      }
      .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 16px;
      }
      .form-actions {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-top: 20px;
      }
      @media (max-width: 900px) {
        .form-grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class DossierFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(DossierService);
  private router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly NATURE_LABELS = NATURE_LABELS;

  form = this.fb.nonNullable.group({
    dateOuverture: ['', Validators.required],
    nature: ['pension_retraite' as TypeContentieuxNature, Validators.required],
    montantReclame: [0],
  });

  isSubmitting = signal(false);

  ngOnInit(): void {
    if (!this.authService.canMutate()) {
      this.router.navigateByUrl('/dossiers');
      return;
    }
    // Defensive reset to ensure no residual values (especially numeroDossier) are present when opening the creation form
    this.form.reset({ dateOuverture: '', nature: 'pension_retraite', montantReclame: 0 });
  }

  enregistrer() {
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    if (this.isSubmitting()) return;
    this.isSubmitting.set(true);
    this.api.create({
      dateOuverture: v.dateOuverture,
      montantReclame: v.montantReclame,
      nature: v.nature,
    }).pipe(finalize(() => this.isSubmitting.set(false))).subscribe({
      next: () => this.router.navigateByUrl('/dossiers'),
      error: (err) => {
        console.error('Erreur création dossier', err);
        alert('Erreur lors de la création du dossier : ' + (err?.error?.message || err?.statusText || 'Erreur inconnue'));
      }
    });
  }
}
