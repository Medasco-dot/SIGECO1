import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { signal } from '@angular/core';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="login-shell">
      <aside class="login-panel" aria-label="Présentation CARFO">
        <div class="brand-panel">
          <p class="brand-role">Service Contentieux et Juridique</p>
          <h1>CARFO</h1>
          <p class="brand-copy">Suivi sécurisé des dossiers contentieux et des décisions juridiques.</p>
        </div>

        <div class="registry-motif" aria-hidden="true">
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
          <span></span>
        </div>
      </aside>

      <section class="form-panel" aria-labelledby="login-title">
        <div class="form-card">
          <div class="form-header">
            <p class="eyebrow">Connexion</p>
            <h2 id="login-title">Accès au portail</h2>
            <p>Entrez vos identifiants CARFO pour poursuivre.</p>
          </div>

          <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
            <label for="identifiant">
              Identifiant
              <input id="identifiant" type="text" formControlName="identifiant" autocomplete="username" />
            </label>

            <label for="motDePasse">
              Mot de passe
              <input id="motDePasse" type="password" formControlName="motDePasse" autocomplete="current-password" />
            </label>

            <p class="error-message" *ngIf="errorMessage" role="alert">{{ errorMessage }}</p>

            <button class="submit-button" type="submit" [disabled]="form.invalid || isSubmitting">
              Se connecter
            </button>
          </form>
        </div>
      </section>
    </section>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; }
      .login-shell { min-height: 100vh; display: grid; grid-template-columns: minmax(320px, 45%) 1fr; background: #f7f5f1; }
      .login-panel { background: #1f3864; color: white; padding: 48px 40px; display: flex; flex-direction: column; justify-content: center; gap: 36px; }
      .brand-panel { max-width: 320px; }
      .brand-role { margin: 0 0 18px; color: rgba(255,255,255,0.72); font-size: 0.95rem; letter-spacing: 0.08em; text-transform: uppercase; font-family: ui-serif, Georgia, 'Times New Roman', serif; }
      h1 { margin: 0; font-size: clamp(2.5rem, 4vw, 4rem); line-height: 0.95; font-family: ui-serif, Georgia, 'Times New Roman', serif; letter-spacing: -0.05em; }
      .brand-copy { margin: 24px 0 0; max-width: 340px; color: rgba(255,255,255,0.86); line-height: 1.75; font-size: 1rem; font-family: Inter, 'Segoe UI', sans-serif; }
      .registry-motif { display: grid; gap: 12px; width: 100%; }
      .registry-motif span { display: block; height: 2px; width: 100%; max-width: 82%; background: rgba(255,255,255,0.16); border-radius: 999px; }
      .form-panel { display: flex; align-items: center; justify-content: center; padding: 48px 40px; }
      .form-card { width: min(100%,380px); background: white; border-radius: 24px; padding: 32px; box-shadow: 0 22px 68px rgba(17,33,66,0.12); border: 1px solid rgba(31,56,100,0.08); }
      .form-header .eyebrow { margin: 0 0 10px; color: #6b7280; font-size: 0.82rem; letter-spacing: 0.12em; text-transform: uppercase; font-weight:700; font-family: Inter, 'Segoe UI', sans-serif; }
      .form-header h2 { margin: 0 0 10px; font-size: 1.75rem; line-height:1.1; color:#2a2e33; font-family: ui-serif, Georgia, 'Times New Roman', serif; }
      form { display: grid; gap: 18px; }
      label { display: grid; gap: 8px; font-size: 0.95rem; font-weight: 700; color: #2a2e33; font-family: Inter, 'Segoe UI', sans-serif; }
      input { border: 1px solid #dfe4e8; border-radius: 12px; padding: 14px 16px; font-size: 1rem; font-family: Inter, 'Segoe UI', sans-serif; color: #2a2e33; background: #ffffff; transition: border-color 0.15s ease, box-shadow 0.15s ease; }
      input:focus { outline: none; border-color: #1f3864; box-shadow: 0 0 0 4px rgba(31,56,100,0.12); }
      .error-message { margin:0; color: #b3261e; font-size: 0.95rem; line-height:1.5; font-family: Inter, 'Segoe UI', sans-serif; }
      .submit-button { background: #1f3864; color: white; border: none; border-radius: 12px; padding: 14px 18px; font-size: 1rem; font-weight: 700; cursor: pointer; transition: transform 0.15s ease, background-color 0.15s ease; font-family: Inter, 'Segoe UI', sans-serif; }
      .submit-button:hover:not(:disabled) { transform: translateY(-1px); }
      .submit-button:disabled { background: #5f6b89; cursor: not-allowed; }
      @media (max-width: 640px) { .login-shell { grid-template-columns: 1fr; min-height: auto; } .login-panel { padding: 28px 24px; min-height: 180px; } .form-panel { padding: 28px 24px 40px; } }
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

  isSubmitting = false;
  errorMessage = '';

  submit() {
    if (this.form.invalid || this.isSubmitting) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = '';
    this.isSubmitting = true;

    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/tableau-de-bord']),
      error: (err: any) => {
        this.errorMessage = 'Identifiant ou mot de passe invalide';
        this.isSubmitting = false;
        console.error('Échec de la connexion', err);
      },
    });
  }
}
