import { Component, EventEmitter, inject, Input, Output, OnChanges, SimpleChanges, signal, effect } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FileUploadModule } from 'primeng/fileupload';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Container, UpdateContainerRequest } from '@shared/types/container';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { ContainerStore } from '../../../../core/store/container.store';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-container-update-modal',
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
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.css'
})
export class ContainerUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() container!: Container;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(ContainerStore);

  private isUpdating = signal(false);
  selectedFile = signal<File | null>(null);
  previewUrl = signal<string | null>(null);
  currentIconUrl = signal<string | null>(null);

  containerForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    displayName: ['', [Validators.required, Validators.maxLength(150)]],
    icon: ['', [Validators.maxLength(100)]],
    orderIndex: [0, [Validators.required, Validators.min(0)]]
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
    if (changes['visible'] && this.visible && this.container) {
      this.loadContainerData(this.container.publicId);
    }
  }

  loadContainerData(publicId: string): void {
    this.store.getForUpdate(publicId).subscribe((response) => {
      if (response.success) {
        const containerData = response.data;
        this.containerForm.patchValue({
          name: containerData.name,
          displayName: containerData.displayName,
          icon: containerData.icon || '',
          orderIndex: containerData.orderIndex
        });
        this.currentIconUrl.set(containerData.iconUrl || null);
        this.previewUrl.set(containerData.iconUrl || null);
        this.selectedFile.set(null);
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.containerForm.reset();
    this.selectedFile.set(null);
    this.previewUrl.set(null);
    this.currentIconUrl.set(null);
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
    if (this.currentIconUrl()) {
      this.previewUrl.set(this.currentIconUrl());
    } else {
      this.previewUrl.set(null);
    }
  }

  updateContainer() {
    if (this.containerForm.invalid) {
      this.containerForm.markAllAsTouched();
      return;
    }

    const request: UpdateContainerRequest = {
      name: this.containerForm.value.name!,
      displayName: this.containerForm.value.displayName!,
      icon: this.containerForm.value.icon || null,
      iconUrl: this.selectedFile() ? null : this.currentIconUrl(), // Mantener el actual si no hay nuevo archivo
      orderIndex: this.containerForm.value.orderIndex!
    };

    this.isUpdating.set(true);
    this.store.updateWithImage({ publicId: this.container.publicId, request, file: this.selectedFile() });
  }
}

