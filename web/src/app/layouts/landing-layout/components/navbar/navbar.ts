import { Component, signal, HostListener, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { NgClass } from '@angular/common';
import { AuthStore } from '@core/store/auth.store';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, NgClass],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class Navbar implements OnInit {
  protected authStore = inject(AuthStore);
  private router = inject(Router);

  mobileMenuOpen = signal(false);
  isScrolled = signal(false);

  @HostListener('window:scroll')
  onWindowScroll(): void {
    this.isScrolled.set(window.scrollY > 10);
  }

  ngOnInit(): void {
    this.isScrolled.set(false);
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update(value => !value);
  }

  login(): void {
    this.router.navigate(['/login']);
  }
}
