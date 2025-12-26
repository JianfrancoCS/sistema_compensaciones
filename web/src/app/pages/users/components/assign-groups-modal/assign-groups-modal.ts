import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { UserStore } from '@core/store/user.store';
import { UserService } from '@core/services/user.service';
import { GroupAssignmentStatusDTO } from '@shared/types/security';
import { CheckboxModule } from 'primeng/checkbox';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-assign-groups-modal',
  standalone: true,
  imports: [
    CommonModule,
    ModalTemplateComponent,
    CheckboxModule,
    FormsModule
  ],
  templateUrl: './assign-groups-modal.html',
  styleUrl: './assign-groups-modal.css'
})
export class AssignGroupsModal {
  @Input() visible: boolean = false;
  @Input() userId: string = '';
  @Input() username: string = '';
  @Output() onHide = new EventEmitter<void>();

  private userService = inject(UserService);
  protected userStore = inject(UserStore);

  protected groupsStatus = signal<GroupAssignmentStatusDTO[]>([]);
  protected loading = signal(false);
  private isUpdating = signal(false);

  constructor() {
    effect(() => {
      if (this.visible && this.userId) {
        this.loadGroupsStatus();
      }
    });

    effect(() => {
      if (!this.isUpdating()) {
        return;
      }

      if (!this.userStore.loading()) {
        if (!this.userStore.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
      }
    });
  }

  private loadGroupsStatus() {
    this.loading.set(true);
    this.userService.getUserGroupStatus(this.userId).subscribe({
      next: (response) => {
        if (response.success) {
          this.groupsStatus.set(response.data);
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
    this.groupsStatus.set([]);
  }

  toggleGroup(groupId: string) {
    const groups = this.groupsStatus();
    const updatedGroups = groups.map(g =>
      g.id === groupId ? { ...g, assigned: !g.assigned } : g
    );
    this.groupsStatus.set(updatedGroups);
  }

  saveGroups() {
    const selectedGroupIds = this.groupsStatus()
      .filter(g => g.assigned)
      .map(g => g.id);

    this.isUpdating.set(true);
    this.userStore.syncUserGroups({ userId: this.userId, groupIds: selectedGroupIds });
  }
}