import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { RoleStore } from '@core/store/role.store';
import { GroupService } from '@core/services/group.service';
import { PermissionAssignmentStatusDTO } from '@shared/types/security';
import { CheckboxModule } from 'primeng/checkbox';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-assign-permissions-modal',
  standalone: true,
  imports: [
    CommonModule,
    ModalTemplateComponent,
    CheckboxModule,
    FormsModule
  ],
  templateUrl: './assign-permissions-modal.html',
  styleUrl: './assign-permissions-modal.css'
})
export class AssignPermissionsModal {
  @Input() visible: boolean = false;
  @Input() roleId: string = '';
  @Input() roleName: string = '';
  @Output() onHide = new EventEmitter<void>();

  private groupService = inject(GroupService);
  protected roleStore = inject(RoleStore);

  protected permissionsStatus = signal<PermissionAssignmentStatusDTO[]>([]);
  protected loading = signal(false);
  private isUpdating = signal(false);

  constructor() {
    effect(() => {
      if (this.visible && this.roleId) {
        this.loadPermissionsStatus();
      }
    });

    effect(() => {
      if (!this.isUpdating()) {
        return;
      }

      if (!this.roleStore.loading()) {
        if (!this.roleStore.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
      }
    });
  }

  private loadPermissionsStatus() {
    this.loading.set(true);
    this.groupService.getGroupPermissionStatus(this.roleId).subscribe({
      next: (response) => {
        if (response.success) {
          this.permissionsStatus.set(response.data);
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.permissionsStatus.set([]);
  }

  togglePermission(permissionId: string) {
    const permissions = this.permissionsStatus();
    const updatedPermissions = permissions.map(p =>
      p.id === permissionId ? { ...p, assigned: !p.assigned } : p
    );
    this.permissionsStatus.set(updatedPermissions);
  }

  savePermissions() {
    const selectedPermissionIds = this.permissionsStatus()
      .filter(p => p.assigned)
      .map(p => p.id);

    this.isUpdating.set(true);
    this.roleStore.syncRolePermissions({ groupId: this.roleId, permissionIds: selectedPermissionIds });
  }
}