import {Component, signal, inject, OnInit} from '@angular/core';
import { AvatarModule} from 'primeng/avatar';
import {CommonModule} from '@angular/common';
import {OverlayBadgeModule} from 'primeng/overlaybadge';
import {BadgeModule} from 'primeng/badge';
import { Router } from '@angular/router';
import { UserService, UserProfile } from '@core/services/user.service';
import { AuthStore } from '@core/store/auth.store';

interface CustomMenuItem {
  label?: string;
  icon?: string;
  separator?: boolean;
  command?: () => void;
}

@Component({
  selector: 'app-user-dropdown',
  imports: [
    AvatarModule,
    CommonModule,
    OverlayBadgeModule,
    BadgeModule
  ],
  templateUrl: './user-dropdown.html',
  standalone: true,
  styleUrl: './user-dropdown.css'
})
export class UserDropdown implements OnInit {
  private userService = inject(UserService);
  private authStore = inject(AuthStore);
  private router = inject(Router);

  isOpen = signal<boolean>(false);
  userInfo = signal<UserProfile>({
    fullName: 'Cargando...',
    documentNumber: '...',
    positionName: '...',
    subsidiaryName: '...'
  });

  menuItems: CustomMenuItem[] = [
    {
      label: 'Mi Perfil',
      icon: 'pi pi-user',
      command: () => this.goToProfile()
    },
    {
      label: 'Cambiar Contraseña',
      icon: 'pi pi-key',
      command: () => this.goToChangePassword()
    },
    {
      separator: true
    },
    {
      label: 'Salir',
      icon: 'pi pi-sign-out',
      command: () => this.logout()
    }
  ];

  async ngOnInit() {
    const profile = await this.userService.getUserProfile();
    this.userInfo.set(profile);
  }

  toggleDropdown(): void {
    this.isOpen.update(value => !value);
  }

  closeDropdown(): void {
    this.isOpen.set(false);
  }

  goToProfile(): void {
    this.closeDropdown();
    this.router.navigate(['/system/my-profile']);
  }

  goToChangePassword(): void {
    this.closeDropdown();
    this.router.navigate(['/system/my-profile/change-password']);
  }

  async logout(): Promise<void> {
    this.closeDropdown();
    await this.authStore.logout();
  }
}
