import { Component, EventEmitter, Input, Output, inject, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { SubsidiarySignerStore } from '../../../../core/store/subsidiary-signer.store';
import { EmployeeSearchStore } from '../../../../core/store/employee-search.store';
import { SubsidiarySignerListDTO } from '../../../../core/services/subsidiary-signer.service';

@Component({
  selector: 'app-assign-signer-create-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ModalTemplateComponent,
    InputTextModule,
    TextareaModule,
    ButtonModule,
    BadgeModule,
    MessageModule,
    ToastModule,
    TooltipModule
  ],
  providers: [MessageService],
  templateUrl: './assign-signer-create-modal.html',
  styleUrls: ['./assign-signer-create-modal.css']
})
export class AssignSignerCreateModalComponent {
  private _visible = false;
  @Input() set visible(value: boolean) {
    this._visible = value;
    if (value) {
      this.initializeOnOpen();
    } else {
      this.cleanupOnClose();
    }
  }
  get visible(): boolean { return this._visible; }
  @Input() subsidiary: SubsidiarySignerListDTO | null = null;
  @Input() existingSignerPublicId: string | null = null; // Para cargar datos existentes si se está editando
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected signerStore = inject(SubsidiarySignerStore);
  protected employeeSearchStore = inject(EmployeeSearchStore);

  readonly foundEmployee = this.employeeSearchStore.foundEmployee;
  readonly searchingEmployee = this.employeeSearchStore.searching;
  readonly employeeSearchError = this.employeeSearchStore.error;
  readonly uploadingImage = this.signerStore.uploadingImage;
  readonly signatureImagePreview = this.signerStore.signatureImagePreview;
  readonly signatureImageUrl = this.signerStore.signatureImageUrl;
  readonly signatureImageFile = this.signerStore.signatureImageFile;
  readonly storeLoading = this.signerStore.loading;
  readonly isSubmitting = signal(false);
  
  readonly isProcessing = computed(() => {
    return this.isSubmitting() || this.uploadingImage() || this.storeLoading();
  });

  signerForm = this.fb.group({
    employeeDocumentNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{8,9}$/)]],
    responsiblePosition: ['', [Validators.required, Validators.maxLength(100)]],
    notes: ['', Validators.maxLength(500)]
  });

  readonly isSubmitDisabled = computed(() => {
    if (this.isProcessing()) return true;
    
    if (!this.foundEmployee()) return true;
    
    const hasImage = !!(this.signatureImageFile() || this.signatureImageUrl());
    if (!hasImage) return true;
    
    const responsiblePosition = this.signerForm.get('responsiblePosition')?.value;
    if (!responsiblePosition || responsiblePosition.trim() === '') return true;
    
    return false;
  });

  constructor() {
    effect(() => {
      const position = this.signerStore.employeePosition();
      if (this._visible && position) {
        this.signerForm.patchValue({ responsiblePosition: position });
      }
    });
    
    effect(() => {
      const foundEmployee = this.foundEmployee();
      if (this._visible && foundEmployee && foundEmployee.position) {
        const currentPosition = this.signerForm.get('responsiblePosition')?.value;
        if (!currentPosition || currentPosition.trim() === '') {
          this.signerForm.patchValue({ responsiblePosition: foundEmployee.position });
        }
      }
    });
    
    effect(() => {
      if (this._visible && this.existingSignerPublicId && this.subsidiary?.subsidiaryPublicId) {
        this.signerStore.getSignerBySubsidiary(this.subsidiary.subsidiaryPublicId);
      }
    });
    
    effect(() => {
      const details = this.signerStore.currentSignerDetails();
      if (details && this._visible && details.signatureImageUrl) {
        this.signerStore.setSignatureImageUrl(details.signatureImageUrl);
        this.signerForm.patchValue({ 
          responsiblePosition: details.responsiblePosition || '', 
          notes: details.notes || '' 
        });
      }
    });
    
    effect(() => {
      const submitting = this.isSubmitting();
      const loading = this.storeLoading();
      const uploading = this.uploadingImage();
      const error = this.signerStore.error();
      
      if (submitting && !loading && !uploading) {
        if (!error) {
          this.hideModal();
        }
        this.isSubmitting.set(false);
      }
    });
  }

  private initializeOnOpen(): void {
    this.signerForm.reset({ employeeDocumentNumber: '', responsiblePosition: '', notes: '' });
    this.signerForm.markAsPristine();
    this.employeeSearchStore.clearSearch();
    this.signerStore.clearSignatureImage();
    this.signerStore.clearEmployeePosition();
    if (this.subsidiary?.subsidiaryPublicId && this.subsidiary.responsiblePosition) {
      this.signerForm.patchValue({ responsiblePosition: this.subsidiary.responsiblePosition });
    }
  }

  private cleanupOnClose(): void {
    this.signerForm.reset({ employeeDocumentNumber: '', responsiblePosition: '', notes: '' });
    this.signerForm.markAsPristine();
    this.employeeSearchStore.clearSearch();
    this.signerStore.clearSignatureImage();
    this.signerStore.clearEmployeePosition();
    this.clearSelectedImage();
  }

  hideModal(): void {
    this.onHide.emit();
  }

  onEmployeeSearch(): void {
    const docNumber = this.signerForm.get('employeeDocumentNumber')?.value;
    if (docNumber && this.signerForm.get('employeeDocumentNumber')?.valid) {
      this.employeeSearchStore.searchByDocumentNumber(docNumber);
    }
  }

  clearEmployeeSearch(): void {
    this.employeeSearchStore.clearSearch();
    this.signerForm.patchValue({ employeeDocumentNumber: '' });
    this.signerForm.get('employeeDocumentNumber')?.markAsUntouched();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    if (files && files.length > 0) {
      const file = files[0];
      this.signerStore.setSignatureImageFile(file);
    }
  }

  clearSelectedImage(): void {
    this.signerStore.clearSignatureImage();
    const fileInput = document.getElementById('signatureImageInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  clearExistingImage() {
    this.signerStore.clearSignatureImage();
    this.clearSelectedImage();
  }

  assignSigner(): void {
    if (this.isSubmitDisabled()) return;
    
    this.isSubmitting.set(true);
    const formValue = this.signerForm.getRawValue();
    const imageFile = this.signatureImageFile();
    const existingImageUrl = this.signatureImageUrl();
    
    this.signerStore.createSigner({
      subsidiaryPublicId: this.subsidiary?.subsidiaryPublicId || null,
      responsibleEmployeeDocumentNumber: formValue.employeeDocumentNumber!,
      responsiblePosition: formValue.responsiblePosition!,
      signatureImageUrl: existingImageUrl || null, // Mantener URL existente si no hay archivo nuevo
      notes: formValue.notes || null
    });
  }
}