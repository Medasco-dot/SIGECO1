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
      <div class="login-card">
        <div class="login-header">
          <div class="login-logo">S</div>
          <h1>SIGECO</h1>
          <p>CARFO — Service Contentieux et Juridique</p>
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
        </form>

        <p class="form-hint">Accès réservé au personnel du Service Contentieux et Juridique de la CARFO.</p>
      </div>
    </section>
  `,
  styles: [
    `
      :host { display: block; min-height: 100vh; }
      .login-shell {
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--carfo-green);
        padding: 24px;
      }

      .login-card {
        width: 100%;
        max-width: 380px;
        background: white;
        border-radius: 8px;
        padding: 32px;
        border: 1px solid var(--carfo-line, #dfe6e1);
      }

      .login-header { text-align: center; margin-bottom: 24px; }
      .login-logo {
        width: 44px; height: 44px; margin: 0 auto 12px;
        border-radius: 8px; background: var(--carfo-green);
        color: white; display: grid; place-items: center;
        font-weight: 800; font-size: 20px;
      }
      .login-header h1 { margin: 0; font-size: 1.6rem; color: var(--carfo-ink, #16211a); }
      .login-header p { margin: 4px 0 0; font-size: 0.85rem; color: var(--carfo-muted, #6b7573); }

      form { display: grid; gap: 14px; }
      label { display: grid; gap: 6px; font-size: 0.9rem; font-weight: 600; color: var(--carfo-ink, #16211a); }
      input { border: 1px solid var(--carfo-line, #dfe6e1); border-radius: 6px; padding: 10px 12px; font-size: 1rem; color: var(--carfo-ink, #16211a); background: white; }
      input:focus { outline: 2px solid var(--carfo-green, #0f8a3c); outline-offset: 1px; }
      .error-message { margin: 0; color: var(--carfo-red, #c02b2b); font-size: 0.9rem; }
      .submit-button { background: var(--carfo-green, #0f8a3c); color: white; border: none; border-radius: 6px; padding: 11px 16px; font-size: 1rem; font-weight: 700; cursor: pointer; margin-top: 4px; }
      .submit-button:hover:not(:disabled) { background: var(--carfo-green-dark, #0a5f29); }
      .submit-button:disabled { background: #9aa79f; cursor: not-allowed; }
      .form-hint { margin: 16px 0 0; font-size: 0.8rem; color: var(--carfo-muted, #6b7573); text-align: center; }
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
