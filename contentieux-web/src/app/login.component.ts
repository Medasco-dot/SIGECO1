import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="login-shell">
      <aside class="login-panel" aria-label="Présentation">
        <div class="brand-panel">
          <p class="brand-role">Service Contentieux et Juridique</p>
          <h1>SIGECO</h1>
          <p class="brand-baseline">par CARFO</p>
          <p class="brand-copy">
            Un espace clair pour suivre chaque dossier contentieux, de son ouverture
            jusqu'à sa résolution — audiences, décisions, pièces et acteurs réunis
            au même endroit.
          </p>
        </div>

        <div class="registry-motif" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
          <span class="accent"></span>
        </div>

        <p class="brand-footer">Caisse Autonome de Retraite des Fonctionnaires — Burkina Faso</p>
      </aside>

      <section class="form-panel" aria-labelledby="login-title">
        <div class="form-card">
          <div class="form-header">
            <p class="eyebrow">Connexion</p>
            <h2 id="login-title">Heureux de vous revoir</h2>
            <p>Entrez vos identifiants pour accéder à vos dossiers.</p>
          </div>

          <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
            <label for="identifiant">
              Identifiant
              <input id="identifiant" type="text" formControlName="identifiant" autocomplete="username" placeholder="ex. amed.sawadogo" />
            </label>

            <label for="motDePasse">
              Mot de passe
              <input id="motDePasse" type="password" formControlName="motDePasse" autocomplete="current-password" placeholder="••••••••" />
            </label>

            <p class="error-message" *ngIf="errorMessage()" role="alert">{{ errorMessage() }}</p>

            <button class="submit-button" type="submit" [disabled]="form.invalid || isSubmitting()">
              {{ isSubmitting() ? 'Connexion en cours…' : 'Se connecter' }}
            </button>

            <p class="form-hint">Accès réservé au personnel du Service Contentieux et Juridique de la CARFO.</p>
          </form>
        </div>
      </section>
    </section>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; }
      .login-shell { min-height: 100vh; display: grid; grid-template-columns: minmax(320px, 45%) 1fr; background: var(--carfo-paper, #f5f8f6); }

      .login-panel {
        position: relative;
        background:
          linear-gradient(160deg, rgba(10,95,41,0.80) 0%, rgba(15,138,60,0.75) 100%),
          url('/images/login-hero.jpg') center / cover no-repeat,
          linear-gradient(160deg, var(--carfo-green-dark, #0a5f29) 0%, var(--carfo-green, #0f8a3c) 100%);
        color: white;
        padding: 48px 44px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: 28px;
        overflow: hidden;
      }
      .login-panel::after {
        content: '';
        position: absolute;
        inset: auto -20% -30% auto;
        width: 60%;
        aspect-ratio: 1;
        border-radius: 50%;
        background: radial-gradient(circle, rgba(201,150,47,0.16) 0%, rgba(201,150,47,0) 70%);
        pointer-events: none;
      }

      .brand-panel { max-width: 340px; position: relative; z-index: 1; }
      .brand-role { margin: 0 0 14px; color: rgba(255,255,255,0.75); font-size: 0.9rem; letter-spacing: 0.1em; text-transform: uppercase; font-family: Inter, 'Segoe UI', sans-serif; }
      h1 { margin: 0; font-size: clamp(2.75rem, 4vw, 4.25rem); line-height: 0.95; font-family: ui-serif, Georgia, 'Times New Roman', serif; letter-spacing: -0.03em; }
      .brand-baseline { margin: 6px 0 0; font-size: 1rem; color: rgba(255,255,255,0.8); font-family: Inter, 'Segoe UI', sans-serif; }
      .brand-copy { margin: 22px 0 0; max-width: 340px; color: rgba(255,255,255,0.88); line-height: 1.7; font-size: 1rem; font-family: Inter, 'Segoe UI', sans-serif; }

      .registry-motif { display: grid; gap: 12px; width: 100%; max-width: 260px; position: relative; z-index: 1; }
      .registry-motif span { display: block; height: 3px; width: 100%; background: rgba(255,255,255,0.2); border-radius: 999px; }
      .registry-motif span.accent { background: var(--carfo-gold, #c9962f); width: 45%; }

      .brand-footer { margin: 0; font-size: 0.82rem; color: rgba(255,255,255,0.65); position: relative; z-index: 1; }

      .form-panel { display: flex; align-items: center; justify-content: center; padding: 48px 40px; }
      .form-card { width: min(100%,400px); background: white; border-radius: 20px; padding: 36px; box-shadow: 0 20px 60px rgba(15,50,30,0.1); border: 1px solid var(--carfo-line, #dfe6e1); }
      .form-header .eyebrow { margin: 0 0 10px; color: var(--carfo-green, #0f8a3c); font-size: 0.82rem; letter-spacing: 0.12em; text-transform: uppercase; font-weight:700; font-family: Inter, 'Segoe UI', sans-serif; }
      .form-header h2 { margin: 0 0 8px; font-size: 1.6rem; line-height:1.2; color: var(--carfo-ink, #16211a); font-family: ui-serif, Georgia, 'Times New Roman', serif; }
      .form-header p { margin: 0; color: var(--carfo-muted, #6b7573); font-size: 0.95rem; }
      form { display: grid; gap: 18px; margin-top: 26px; }
      label { display: grid; gap: 8px; font-size: 0.9rem; font-weight: 600; color: var(--carfo-ink, #16211a); font-family: Inter, 'Segoe UI', sans-serif; }
      input { border: 1px solid var(--carfo-line, #dfe6e1); border-radius: 10px; padding: 13px 15px; font-size: 1rem; font-family: Inter, 'Segoe UI', sans-serif; color: var(--carfo-ink, #16211a); background: #fbfdfc; transition: border-color 0.15s ease, box-shadow 0.15s ease; }
      input:focus { outline: none; border-color: var(--carfo-green, #0f8a3c); box-shadow: 0 0 0 4px rgba(15,138,60,0.12); background: white; }
      .error-message { margin:0; color: var(--carfo-red, #c02b2b); font-size: 0.9rem; line-height:1.5; font-family: Inter, 'Segoe UI', sans-serif; }
      .submit-button { background: var(--carfo-green, #0f8a3c); color: white; border: none; border-radius: 10px; padding: 14px 18px; font-size: 1rem; font-weight: 700; cursor: pointer; transition: transform 0.15s ease, background-color 0.15s ease; font-family: Inter, 'Segoe UI', sans-serif; }
      .submit-button:hover:not(:disabled) { background: var(--carfo-green-dark, #0a5f29); transform: translateY(-1px); }
      .submit-button:disabled { background: #9aa79f; cursor: not-allowed; transform: none; }
      .form-hint { margin: 4px 0 0; font-size: 0.82rem; color: var(--carfo-muted, #6b7573); text-align: center; }

      @media (max-width: 640px) { .login-shell { grid-template-columns: 1fr; min-height: auto; } .login-panel { padding: 32px 24px; min-height: 220px; } .form-panel { padding: 28px 24px 40px; } }
      @media (prefers-reduced-motion: reduce) { *, *::before, *::after { transition-duration: 0.01ms !important; animation-duration: 0.01ms !important; animation-iteration-count: 1 !important; } }
    `
  ]
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  form = this.fb.nonNullable.group({
    identifiant: ['', Validators.required],
    motDePasse: ['', Validators.required],
  });

  isSubmitting = signal(false);
  errorMessage = signal('');

  submit() {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set('');
    this.isSubmitting.set(true);

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/tableau-de-bord']),
      error: (err: any) => {
        this.errorMessage.set('Identifiant ou mot de passe invalide');
        this.isSubmitting.set(false);
        console.error('Échec de la connexion', err);
      },
    });
  }
}
