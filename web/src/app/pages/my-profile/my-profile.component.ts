import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService, EmployeeMeResponse } from '@core/services/auth.service';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { AvatarModule } from 'primeng/avatar';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    CardModule,
    AvatarModule,
    ToastModule
  ],
  templateUrl: './my-profile.component.html',
  styleUrl: './my-profile.component.css',
  providers: [MessageService]
})
export class MyProfileComponent implements OnInit {
  private authService = inject(AuthService);
  private messageService = inject(MessageService);
  private router = inject(Router);

  userInfo = signal<EmployeeMeResponse | null>(null);
  loading = signal(false);

  ngOnInit(): void {
    this.loadUserInfo();
  }

  loadUserInfo(): void {
    this.loading.set(true);
    this.authService.getMyInfo().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.userInfo.set(response.data);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'No se pudo cargar la información del usuario'
          });
        }
        this.loading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar la información del usuario'
        });
        this.loading.set(false);
      }
    });
  }

  goToChangePassword(): void {
    this.router.navigate(['/system/my-profile/change-password']);
  }

  get fullName(): string {
    const info = this.userInfo();
    if (!info) return '-';
    const parts = [info.names, info.paternalLastname, info.maternalLastname].filter(Boolean);
    return parts.length > 0 ? parts.join(' ') : '-';
  }

  get hasEmployeeData(): boolean {
    const info = this.userInfo();
    return info ? !!(info.names || info.paternalLastname || info.positionName) : false;
  }

  getInitials(): string {
    const info = this.userInfo();
    if (!info) return '?';
    
    const names = info.names || '';
    const paternal = info.paternalLastname || '';
    
    if (names && paternal) {
      return (names.charAt(0) + paternal.charAt(0)).toUpperCase();
    } else if (names) {
      return names.charAt(0).toUpperCase();
    } else if (info.documentNumber) {
      return info.documentNumber.charAt(0);
    }
    
    return '?';
  }

  getAge(): number | null {
    const info = this.userInfo();
    if (!info || !info.dateOfBirth) return null;
    
    const birthDate = new Date(info.dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return age;
  }
}

