import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthStore } from '@core/store/auth.store';
import {Button} from 'primeng/button';

@Component({
  selector: 'app-unauthorized',
  imports: [CommonModule, Button],
  templateUrl: './unauthorized.html',
  standalone: true
})
export class Unauthorized {
  private router = inject(Router);
  protected authStore = inject(AuthStore);

  goHome() {
    this.router.navigate(['/']);
  }

  async logout() {
    await this.authStore.logout();
  }
}
