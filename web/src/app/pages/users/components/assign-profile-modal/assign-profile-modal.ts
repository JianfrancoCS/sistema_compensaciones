import { Component, effect, EventEmitter, inject, Input, Output, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { UserStore } from '@core/store/user.store';
import { ProfileStore } from '@core/store/profile.store';
import { SelectModule } from 'primeng/select';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-assign-profile-modal',
  standalone: true,
  imports: [
    CommonModule,
    ModalTemplateComponent,
    SelectModule,
    ReactiveFormsModule
  ],
  templateUrl: './assign-profile-modal.html',
  styleUrl: './assign-profile-modal.css'
})
export class AssignProfileModal {
  @Input() visible: boolean = false;
  @Input() userId: string = '';
  @Input() username: string = '';
  @Input() currentProfileId: string | null = null;
  @Output() onHide = new EventEmitter<void>();

  protected userStore = inject(UserStore);
  protected profileStore = inject(ProfileStore);
  private fb = inject(FormBuilder);

  protected loading = signal(false);
  private isUpdating = signal(false);

  profileForm = this.fb.group({
    profileId: [null as string | null, Validators.required]
  });

  profileOptions = computed(() => {
    return this.profileStore.profiles().map(profile => ({
      label: profile.name,
      value: profile.publicId
    }));
  });

  constructor() {
    effect(() => {
      if (this.visible) {
        this.profileStore.loadAllForSelect();
        this.profileForm.patchValue({ profileId: this.currentProfileId });
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

  hideModal() {
    this.onHide.emit();
    this.profileForm.reset();
  }

  saveProfile() {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const profileId = this.profileForm.value.profileId;
    if (!profileId) {
      return;
    }

    this.isUpdating.set(true);
    this.userStore.assignProfile({
      userId: this.userId,
      request: { profileId }
    });
  }
}

