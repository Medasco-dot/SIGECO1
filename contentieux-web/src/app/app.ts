import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './auth.service';

const PAGE_TITLES: { match: string; title: string }[] = [
  { match: '/tableau-de-bord', title: 'Tableau de bord' },
  { match: '/dossiers/nouveau', title: 'Nouveau dossier' },
  { match: '/dossiers/', title: 'Fiche dossier' },
  { match: '/dossiers', title: 'Dossiers' },
  { match: '/audiences', title: 'Audiences et décisions' },
  { match: '/parties', title: 'Parties' },
  { match: '/juristes', title: 'Juristes' },
  { match: '/cabinets', title: 'Cabinets' },
  { match: '/documents', title: 'Documents' },
  { match: '/utilisateurs', title: 'Utilisateurs' },
];

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly authService = inject(AuthService);
  protected readonly router = inject(Router);

  protected readonly menuOpen = signal(false);
  protected readonly userMenuOpen = signal(false);
  protected readonly currentUrl = signal(this.router.url);

  constructor() {
    this.router.events.subscribe((e) => {
      if (e instanceof NavigationEnd) {
        this.currentUrl.set(e.urlAfterRedirects);
        this.userMenuOpen.set(false);
      }
    });
  }

  isLoginRoute() {
    return this.router.url.startsWith('/login');
  }

  pageTitle() {
    const url = this.currentUrl();
    const found = PAGE_TITLES.find((p) => url.startsWith(p.match));
    return found?.title ?? 'SIGECO';
  }

  userInitials() {
    const id = this.authService.currentUser()?.identifiant ?? '';
    const parts = id.split(/[.\s_-]+/).filter(Boolean);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return id.substring(0, 2).toUpperCase();
  }

  toggleMenu() {
    this.menuOpen.update((open) => !open);
  }

  closeMenu() {
    this.menuOpen.set(false);
  }

  toggleUserMenu() {
    this.userMenuOpen.update((open) => !open);
  }

  logout() {
    this.closeMenu();
    this.userMenuOpen.set(false);
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
