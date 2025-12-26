import { Component, inject, OnInit, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { DialogModule } from 'primeng/dialog';
import { UserStore } from '@core/store/user.store';
import { PositionStore } from '@core/store/position.store';
import { UserDTO } from '@shared/types/security';
import { DateDisplayComponent } from '@shared/dummys';
import { CreateUserModal } from './components/create-user-modal/create-user-modal';

@Component({
  selector: 'app-users',
  imports: [
    CommonModule, 
    FormsModule,
    TableModule, 
    ButtonModule, 
    InputTextModule, 
    InputGroupModule,
    InputGroupAddonModule,
    SelectModule,
    ToastModule, 
    TooltipModule, 
    DialogModule,
    DateDisplayComponent,
    CreateUserModal
  ],
  templateUrl: './users.html',
  standalone: true,
  styleUrl: './users.css'
})
export class Users implements OnInit {
  userStore = inject(UserStore);
  positionStore = inject(PositionStore);
  private router = inject(Router);

  isDetailsModalVisible = signal(false);
  isCreateUserModalVisible = signal(false);
  selectedPositionId: string | undefined = undefined;
  searchValue = signal<string>('');

  positionSelectOptions = computed(() => {
    const options = this.positionStore.positionSelectOptionsAll().map(pos => ({
      label: pos.name,
      value: pos.publicId
    }));
    return [{ label: 'Todos los cargos', value: undefined }, ...options];
  });

  ngOnInit(): void {
    this.userStore.resetFilters();
    this.userStore.init();
    this.positionStore.init();
    
    effect(() => {
      const positionId = this.userStore.filters().positionId;
      this.selectedPositionId = positionId || undefined;
    });

    effect(() => {
      const search = this.userStore.filters().search;
      this.searchValue.set(search || '');
    });
  }

  loadUsers(event: TableLazyLoadEvent): void {
    this.userStore.onLazyLoad(event);
  }

  openCreateUser(): void {
    this.isCreateUserModalVisible.set(true);
  }

  closeCreateUserModal(): void {
    this.isCreateUserModalVisible.set(false);
  }

  openAssignProfiles(user: UserDTO): void {
    this.router.navigate(['/system/settings/users', user.publicId, 'assign-profiles']);
  }

  toggleUserStatus(user: UserDTO): void {
    this.userStore.updateUserStatus({ userId: user.publicId, isActive: !user.isActive });
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const value = inputElement.value || '';
    this.searchValue.set(value);
    this.userStore.setSearch(value);
    this.userStore.loadUsers();
  }

  clearSearch(): void {
    this.searchValue.set('');
    this.userStore.setSearch('');
    this.userStore.loadUsers();
  }

  onPositionFilterChange(positionId: string | undefined): void {
    this.userStore.setPositionFilter(positionId);
    this.userStore.loadUsers();
  }

  openUserDetails(user: UserDTO): void {
    this.isDetailsModalVisible.set(true);
    this.userStore.loadUserDetails(user.publicId);
  }

  closeDetailsModal(): void {
    this.isDetailsModalVisible.set(false);
    this.userStore.clearUserDetails();
  }
}
