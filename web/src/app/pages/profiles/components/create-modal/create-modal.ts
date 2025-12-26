import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateProfileRequest } from '@shared/types/profile';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { ProfileStore } from '../../../../core/store/profile.store';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-profile-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    TextareaModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class ProfileCreateModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(ProfileStore);

  private isCreating = signal(false);

  profileForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(500)]]
  });

  constructor() {
    effect(() => {
      if (!this.isCreating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isCreating.set(false);
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.profileForm.reset();
    this.store.clearError();
  }

  createProfile() {
    if (this.profileForm.valid) {
      const request: CreateProfileRequest = {
        name: this.profileForm.value.name!,
        description: this.profileForm.value.description || null
      };
      this.isCreating.set(true);
      this.store.create(request);
    }
  }
}

