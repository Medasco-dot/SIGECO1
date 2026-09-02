import { Component, inject, signal, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ReferentielService } from './referentiel.service';
import { JuristeService, Juriste } from './juriste.service';

type Mode = 'parties' | 'juristes' | 'cabinets' | 'utilisateurs';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <section class="page-intro">
      <div>
        <h1>{{ titre() }}</h1>
        <p class="intro">Gérez les référentiels métiers essentiels à la gestion du contentieux.</p>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Ajouter une fiche</h2>
          <p class="muted">Enregistrez une nouvelle entrée dans le référentiel sélectionné.</p>
        </div>
      </div>

      @switch (mode) {
        @case ('parties') {
          <form class="form" [formGroup]="partie" (ngSubmit)="editing() ? updatePartie() : ajouterPartie()">
            <div class="form-grid">
              <label>Nom<input formControlName="nom"/></label>
              <label>Prénom<input formControlName="prenom"/></label>
              <label>CNIB<input formControlName="numeroCnib"/></label>
              <label>Statut matrimonial<select formControlName="statutMatrimonial">
                <option value="">—</option>
                <option value="celibataire">Célibataire</option>
                <option value="marie">Marié(e)</option>
                <option value="divorce">Divorcé(e)</option>
                <option value="veuf">Veuf(ve)</option>
              </select></label>
            </div>
            <div class="row-actions">
              <button [disabled]="partie.invalid">{{ editing() ? 'Enregistrer les modifications' : 'Enregistrer' }}</button>
              @if(editing()) { <button type="button" class="button" (click)="cancelEdit()">Annuler</button> }
            </div>
          </form>
        }
        @case ('juristes') {
          <form class="form" [formGroup]="juriste" (ngSubmit)="editing() ? updateJuriste() : ajouterJuriste()">
            <div class="form-grid">
              <label>Matricule<input formControlName="matricule"/></label>
              <label>Nom<input formControlName="nom"/></label>
              <label>Prénoms<input formControlName="prenoms"/></label>
              <label>Spécialité<select formControlName="specialite">
                <option value="polyvalent">Polyvalent</option>
                <option value="pension_retraite">Pension retraite</option>
                <option value="pension_reversement">Pension reversement</option>
                <option value="acte_carriere">Acte carrière</option>
                <option value="marche_public">Marché public</option>
                <option value="penal">Pénal</option>
              </select></label>
            </div>
            <div class="row-actions">
              <button [disabled]="juriste.invalid">{{ editing() ? 'Enregistrer les modifications' : 'Enregistrer' }}</button>
              @if(editing()) { <button type="button" class="button" (click)="cancelEdit()">Annuler</button> }
            </div>
          </form>
        }
        @case ('cabinets') {
          <form class="form" [formGroup]="cabinet" (ngSubmit)="editing() ? updateCabinet() : ajouterCabinet()">
            <div class="form-grid">
              <label>Identifiant<input formControlName="identifiantCabinet"/></label>
              <label>Nom du cabinet<input formControlName="nomCabinet"/></label>
              <label>Courriel<input formControlName="mail"/></label>
              <label>Téléphone<input formControlName="telephone"/></label>
              <label>Adresse<input formControlName="adresse"/></label>
            </div>
            <div class="row-actions">
              <button [disabled]="cabinet.invalid">{{ editing() ? 'Enregistrer les modifications' : 'Enregistrer' }}</button>
              @if(editing()) { <button type="button" class="button" (click)="cancelEdit()">Annuler</button> }
            </div>
          </form>
        }
        @case ('utilisateurs') {
          <form class="form" [formGroup]="utilisateur" (ngSubmit)="editing() ? updateUtilisateur() : ajouterUtilisateur()">
            <div class="form-grid">
              <label>Identifiant<input formControlName="identifiant"/></label>
              <label>Mot de passe<input type="password" formControlName="motDePasse" placeholder="obligatoire à chaque enregistrement"/></label>
              <label>Nom<input formControlName="nom"/></label>
              <label>Prénom<input formControlName="prenom"/></label>
              <label>Rôle<select formControlName="role">
                <option value="juriste">Juriste</option>
                <option value="chef_service">Chef de service</option>
                <option value="direction_generale">Direction générale</option>
              </select></label>
              @if (utilisateur.value.role === 'juriste') {
                <label>Fiche juriste associée (optionnel)<select formControlName="matriculeJuriste">
                  <option value="">— aucune —</option>
                  @for (j of juristesDisponibles(); track j.matricule) {
                    <option [value]="j.matricule">{{ j.matricule }} - {{ j.nom }} {{ j.prenoms }}</option>
                  }
                </select></label>
              }
            </div>
            <div class="row-actions">
              <button [disabled]="utilisateur.invalid">{{ editing() ? 'Enregistrer les modifications' : 'Enregistrer' }}</button>
              @if(editing()) { <button type="button" class="button" (click)="cancelEdit()">Annuler</button> }
            </div>
          </form>
        }
      }
    </section>

    <section class="panel">
      <div class="panel-head">
        <div>
          <h2>Liste</h2>
          <p class="muted">Fiches disponibles dans le référentiel actuel.</p>
        </div>
      </div>
      <div class="table-shell">
        @if (mode === 'utilisateurs') {
          <table>
            <thead>
              <tr><th>Identifiant</th><th>Nom</th><th>Rôle</th><th>Matricule juriste</th><th></th></tr>
            </thead>
            <tbody>
              @for (item of elements(); track $index) {
                <tr>
                  <td>{{ item.identifiant }}</td>
                  <td>{{ item.nom }} {{ item.prenom }}</td>
                  <td>{{ item.role }}</td>
                  <td>{{ item.matriculeJuriste || '-' }}</td>
                  <td>
                    <button class="link" (click)="startEdit(item)">Modifier</button>
                    <button class="link danger" (click)="supprimer(item)">Supprimer</button>
                  </td>
                </tr>
              }
              @empty {
                <tr><td colspan="5" class="empty">Aucun utilisateur enregistré.</td></tr>
              }
            </tbody>
          </table>
        } @else {
          <table>
            <thead>
              <tr><th>Nom</th><th>Identifiant</th><th>Complément</th><th></th></tr>
            </thead>
            <tbody>
              @for (item of elements(); track $index) {
                <tr>
                  <td>{{ item.nom || item.nomCabinet }}</td>
                  <td>{{ item.matricule || item.numeroCnib || item.identifiantCabinet || '' }}</td>
                  <td>{{ item.prenom || item.prenoms || item.mail || '' }}</td>
                  <td>
                    <button class="link" (click)="startEdit(item)">Modifier</button>
                    <button class="link danger" (click)="supprimer(item)">Supprimer</button>
                  </td>
                </tr>
              }
              @empty {
                <tr><td colspan="4" class="empty">Aucune fiche enregistrée.</td></tr>
              }
            </tbody>
          </table>
        }
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
      .muted { color: #7a8c9a; }
      .table-shell { overflow-x: auto; border: 1px solid #e7ecef; border-radius: 12px; }
      @media (max-width: 900px) {
        .form-grid { grid-template-columns: 1fr; }
      }
    `,
  ],
})
export class ReferentielComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ReferentielService);
  private juristeApi = inject(JuristeService);
  private fb = inject(FormBuilder);
  mode = this.route.snapshot.data['mode'] as Mode;
  titre = signal(
    this.mode === 'parties' ? 'Parties' :
    this.mode === 'juristes' ? 'Juristes' :
    this.mode === 'utilisateurs' ? 'Utilisateurs' :
    'Cabinets d’avocats'
  );
  elements = signal<any[]>([]);
  editing = signal<any | null>(null);
  juristesDisponibles = signal<Juriste[]>([]);

  partie = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    numeroCnib: [''],
    statutMatrimonial: ['']
  });
  juriste = this.fb.nonNullable.group({
    matricule: ['', Validators.required],
    nom: ['', Validators.required],
    prenoms: ['', Validators.required],
    specialite: ['polyvalent']
  });
  cabinet = this.fb.nonNullable.group({
    identifiantCabinet: ['', Validators.required],
    nomCabinet: ['', Validators.required],
    mail: [''],
    telephone: [''],
    adresse: [''],
  });
  utilisateur = this.fb.nonNullable.group({
    identifiant: ['', Validators.required],
    motDePasse: ['', Validators.required],
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    role: ['juriste', Validators.required],
    matriculeJuriste: [''],
  });

  constructor() {
    this.charger();
  }

  ngOnInit() {
    if (this.mode === 'utilisateurs') {
      this.juristeApi.getAll().subscribe((v) => this.juristesDisponibles.set(v));
    }
  }

  charger() {
    this.api.liste<any>(this.mode).subscribe((v) => this.elements.set(v));
  }

  private partiePayload() {
    const v = this.partie.getRawValue();
    return {
      nom: v.nom,
      prenom: v.prenom,
      ...(v.numeroCnib ? { numeroCnib: v.numeroCnib } : {}),
      ...(v.statutMatrimonial ? { statutMatrimonial: v.statutMatrimonial } : {}),
    };
  }

  ajouterPartie() {
    this.api.creer('parties', this.partiePayload()).subscribe({
      next: () => {
        this.partie.reset();
        this.charger();
      },
      error: (e) => alert('Erreur enregistrement partie : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  ajouterJuriste() {
    this.api.creer('juristes', this.juriste.getRawValue()).subscribe({
      next: () => {
        this.juriste.reset({ specialite: 'polyvalent' });
        this.charger();
      },
      error: (e) => alert('Erreur enregistrement juriste : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  ajouterCabinet() {
    const v = this.cabinet.getRawValue();
    const payload: any = {
      identifiantCabinet: v.identifiantCabinet,
      nomCabinet: v.nomCabinet,
      ...(v.mail ? { mail: v.mail } : {}),
      ...(v.telephone ? { telephone: v.telephone } : {}),
      ...(v.adresse ? { adresse: v.adresse } : {}),
    };
    this.api.creer('cabinets', payload).subscribe({
      next: () => {
        this.cabinet.reset();
        this.charger();
      },
      error: (e) => alert('Erreur enregistrement cabinet : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  private utilisateurPayload() {
    const v = this.utilisateur.getRawValue();
    return {
      identifiant: v.identifiant,
      motDePasse: v.motDePasse,
      nom: v.nom,
      prenom: v.prenom,
      role: v.role,
      matriculeJuriste: v.matriculeJuriste || null,
    };
  }

  ajouterUtilisateur() {
    this.api.creer('utilisateurs', this.utilisateurPayload()).subscribe({
      next: () => {
        this.utilisateur.reset({ role: 'juriste' });
        this.charger();
      },
      error: (e) => alert('Erreur enregistrement utilisateur : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  updateUtilisateur() {
    const item = this.editing();
    if (!item) return;
    this.api.update('utilisateurs', item.id, this.utilisateurPayload()).subscribe({
      next: () => {
        this.cancelEdit();
        this.charger();
      },
      error: (e) => alert('Erreur modification utilisateur : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  startEdit(item: any) {
    this.editing.set(item);
    if (this.mode === 'parties') {
      this.partie.patchValue({ nom: item.nom || '', prenom: item.prenom || '', numeroCnib: item.numeroCnib || '', statutMatrimonial: item.statutMatrimonial || '' });
    } else if (this.mode === 'juristes') {
      this.juriste.patchValue({ matricule: item.matricule || '', nom: item.nom || '', prenoms: item.prenoms || '', specialite: item.specialite || 'polyvalent' });
    } else if (this.mode === 'cabinets') {
      this.cabinet.patchValue({ identifiantCabinet: item.identifiantCabinet || '', nomCabinet: item.nomCabinet || '', mail: item.mail || '', telephone: item.telephone || '', adresse: item.adresse || '' });
    } else if (this.mode === 'utilisateurs') {
      this.utilisateur.patchValue({ identifiant: item.identifiant || '', motDePasse: '', nom: item.nom || '', prenom: item.prenom || '', role: item.role || 'juriste', matriculeJuriste: item.matriculeJuriste || '' });
    }
  }

  cancelEdit() {
    this.editing.set(null);
    this.partie.reset();
    this.juriste.reset({ specialite: 'polyvalent' });
    this.cabinet.reset();
    this.utilisateur.reset({ role: 'juriste' });
  }

  updatePartie() {
    const item = this.editing();
    if (!item) return;
    const id = item.id || item.numeroCnib;
    this.api.update('parties', id, this.partiePayload()).subscribe({
      next: () => {
        this.cancelEdit();
        this.charger();
      },
      error: (e) => alert('Erreur modification partie : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  updateJuriste() {
    const item = this.editing();
    if (!item) return;
    const id = item.matricule;
    this.api.update('juristes', id, this.juriste.getRawValue()).subscribe({
      next: () => {
        this.cancelEdit();
        this.charger();
      },
      error: (e) => alert('Erreur modification juriste : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  updateCabinet() {
    const item = this.editing();
    if (!item) return;
    const id = item.identifiantCabinet || item.id;
    this.api.update('cabinets', id, this.cabinet.getRawValue()).subscribe({
      next: () => {
        this.cancelEdit();
        this.charger();
      },
      error: (e) => alert('Erreur modification cabinet : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }

  supprimer(item: any) {
    if (!confirm('Supprimer cet élément du référentiel ?')) return;
    const id = item.id || item.matricule || item.identifiantCabinet || item.numeroCnib;
    this.api.supprimer(this.mode, id).subscribe({
      next: () => this.charger(),
      error: (e) => alert('Erreur suppression : ' + (e?.error?.detail || e?.statusText || 'Erreur inconnue')),
    });
  }
}
