import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FileUploadModule } from 'primeng/fileupload';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateElementRequest } from '@shared/types/element';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { ElementStore } from '../../../../core/store/element.store';
import { ContainerStore } from '../../../../core/store/container.store';
import { MessageModule } from 'primeng/message';
import { CheckboxModule } from 'primeng/checkbox';

@Component({
  selector: 'app-element-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    InputNumberModule,
    SelectModule,
    FileUploadModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule,
    CheckboxModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class ElementCreateModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(ElementStore);
  protected containerStore = inject(ContainerStore);

  private isCreating = signal(false);
  selectedFile = signal<File | null>(null);
  previewUrl = signal<string | null>(null);

  readonly containerOptions = this.containerStore.containers;

  elementForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    displayName: ['', [Validators.required, Validators.maxLength(150)]],
    route: ['', [Validators.maxLength(255)]],
    icon: ['', [Validators.maxLength(100)]],
    containerPublicId: [null as string | null],
    orderIndex: [0, [Validators.required, Validators.min(0)]],
    isWeb: [true],
    isMobile: [true],
    isDesktop: [false]
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
    this.elementForm.reset();
    this.elementForm.patchValue({ 
      orderIndex: 0,
      isWeb: true,
      isMobile: true,
      isDesktop: false
    });
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

  createElement() {
    if (this.elementForm.valid) {
      const request: CreateElementRequest = {
        name: this.elementForm.value.name!,
        displayName: this.elementForm.value.displayName!,
        route: this.elementForm.value.route || null,
        icon: this.elementForm.value.icon || null,
        iconUrl: null,
        containerPublicId: this.elementForm.value.containerPublicId || null,
        orderIndex: this.elementForm.value.orderIndex!,
        isWeb: this.elementForm.value.isWeb ?? true,
        isMobile: this.elementForm.value.isMobile ?? true,
        isDesktop: this.elementForm.value.isDesktop ?? false
      };
      this.isCreating.set(true);
      this.store.createWithImage({ request, file: this.selectedFile() });
    }
  }
}

