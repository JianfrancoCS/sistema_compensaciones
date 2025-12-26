import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { RoleStore } from '@core/store/role.store';
import { CreateRoleModal } from './components/create-role-modal/create-role-modal';
import { AssignPermissionsModal } from './components/assign-permissions-modal/assign-permissions-modal';
import { GroupDTO } from '@shared/types/security';

@Component({
  selector: 'app-roles',
  imports: [CommonModule, TableModule, ButtonModule, InputTextModule, ToastModule, TooltipModule, CreateRoleModal, AssignPermissionsModal],
  templateUrl: './roles.html',
  standalone: true,
  styleUrl: './roles.css'
})
export class Roles implements OnInit {
  roleStore = inject(RoleStore);

  showCreateModal = signal(false);
  showAssignPermissionsModal = signal(false);
  selectedRole = signal<GroupDTO | null>(null);

  ngOnInit(): void {
    this.roleStore.resetFilters();
    this.roleStore.init();
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  openAssignPermissionsModal(role: GroupDTO): void {
    this.selectedRole.set(role);
    this.showAssignPermissionsModal.set(true);
  }

  closeAssignPermissionsModal(): void {
    this.showAssignPermissionsModal.set(false);
    this.selectedRole.set(null);
  }
}