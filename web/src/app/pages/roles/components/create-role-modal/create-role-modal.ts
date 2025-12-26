import { Component, effect, EventEmitter, inject, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { RoleStore } from '@core/store/role.store';
import { PermissionService } from '@core/services/permission.service';
import { MultiSelectModule } from 'primeng/multiselect';
import { InputTextModule } from 'primeng/inputtext';
import { AvailableRoleDTO } from '@shared/types/security';

@Component({
  selector: 'app-create-role-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ModalTemplateComponent,
    MultiSelectModule,
    InputTextModule
  ],
  templateUrl: './create-role-modal.html',
  styleUrl: './create-role-modal.css'
})
export class CreateRoleModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private permissionService = inject(PermissionService);
  protected roleStore = inject(RoleStore);

  protected availablePermissions: AvailableRoleDTO[] = [];
  protected loadingPermissions = false;

  roleForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    permissionIds: [[] as string[], [Validators.required]]
  });

  constructor() {
    effect(() => {
      if (this.visible) {
        this.loadAvailablePermissions();
      }
    });

    effect(() => {
      if (this.roleStore.loading()) {
        return;
      }

      if (!this.roleStore.error() && this.visible) {
        this.hideModal();
      }
    });
  }

  private loadAvailablePermissions() {
    this.loadingPermissions = true;
    this.permissionService.getAvailableRoles().subscribe({
      next: (response) => {
        if (response.success) {
          this.availablePermissions = response.data;
        }
        this.loadingPermissions = false;
      },
      error: () => {
        this.loadingPermissions = false;
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.roleForm.reset();
    this.availablePermissions = [];
  }

  createRole() {
    if (this.roleForm.valid) {
      this.roleStore.createRole({
        name: this.roleForm.value.name!,
        permissionIds: this.roleForm.value.permissionIds!
      });
    }
  }
}