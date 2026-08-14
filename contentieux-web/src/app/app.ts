import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly authService = inject(AuthService);
  protected readonly router = inject(Router);

  isLoginRoute() {
    return this.router.url.startsWith('/login');
  }

  logout() {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
