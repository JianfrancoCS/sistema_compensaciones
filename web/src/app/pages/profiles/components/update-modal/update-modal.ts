import { Component, EventEmitter, inject, Input, Output, OnChanges, SimpleChanges, signal, effect } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Profile, UpdateProfileRequest } from '@shared/types/profile';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { ProfileStore } from '../../../../core/store/profile.store';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-profile-update-modal',
  standalone: true,
  imports: [
    InputTextModule,
    TextareaModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule
  ],
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.css'
})
export class ProfileUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() profile!: Profile;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(ProfileStore);

  private isUpdating = signal(false);

  profileForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(500)]]
  });

  constructor() {
    effect(() => {
      if (!this.isUpdating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.profile) {
      this.loadProfileData(this.profile.publicId);
    }
  }

  loadProfileData(publicId: string): void {
    this.store.getForUpdate(publicId).subscribe((response) => {
      if (response.success) {
        const profileData = response.data;
        this.profileForm.patchValue({
          name: profileData.name,
          description: profileData.description || ''
        });
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.profileForm.reset();
    this.store.clearError();
  }

  updateProfile() {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const request: UpdateProfileRequest = {
      name: this.profileForm.value.name!,
      description: this.profileForm.value.description || null
    };

    this.isUpdating.set(true);
    this.store.update({ publicId: this.profile.publicId, request });
  }
}

