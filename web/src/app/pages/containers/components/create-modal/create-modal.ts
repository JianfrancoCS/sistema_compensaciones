import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FileUploadModule } from 'primeng/fileupload';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateContainerRequest } from '@shared/types/container';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { ContainerStore } from '../../../../core/store/container.store';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-container-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    InputNumberModule,
    FileUploadModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class ContainerCreateModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(ContainerStore);

  private isCreating = signal(false);
  selectedFile = signal<File | null>(null);
  previewUrl = signal<string | null>(null);

  containerForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    displayName: ['', [Validators.required, Validators.maxLength(150)]],
    icon: ['', [Validators.maxLength(100)]],
    orderIndex: [0, [Validators.required, Validators.min(0)]]
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
    this.containerForm.reset();
    this.containerForm.patchValue({ orderIndex: 0 });
    this.selectedFile.set(null);
    this.previewUrl.set(null);
    this.store.clearError();
  }

  onFileSelect(event: any) {
    const files = Array.from(event.files) as File[];
    if (files.length > 0) {
      const file = files[0];
      this.selectedFile.set(file);
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.previewUrl.set(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  }

  removeFile() {
    this.selectedFile.set(null);
    this.previewUrl.set(null);
  }

  createContainer() {
    if (this.containerForm.valid) {
      const request: CreateContainerRequest = {
        name: this.containerForm.value.name!,
        displayName: this.containerForm.value.displayName!,
        icon: this.containerForm.value.icon || null,
        iconUrl: null,
        orderIndex: this.containerForm.value.orderIndex!
      };
      this.isCreating.set(true);
      this.store.createWithImage({ request, file: this.selectedFile() });
    }
  }
}

