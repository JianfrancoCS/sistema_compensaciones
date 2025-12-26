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
import { SubsidiarySignerStore } from '../../../../core/store/subsidiary-signer.store';
import { EmployeeSearchStore } from '../../../../core/store/employee-search.store';
import { SubsidiarySignerListDTO } from '../../../../core/services/subsidiary-signer.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-assign-signer-update-modal',
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
  templateUrl: './assign-signer-update-modal.html',
  styleUrls: ['./assign-signer-update-modal.css']
})
export class AssignSignerUpdateModalComponent {
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
  @Input() signerPublicId: string | null = null;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected signerStore = inject(SubsidiarySignerStore);
  protected employeeSearchStore = inject(EmployeeSearchStore);

  readonly foundEmployee = this.employeeSearchStore.foundEmployee;
  readonly searchingEmployee = this.employeeSearchStore.searching;
  readonly employeeSearchError = this.employeeSearchStore.error;
  readonly uploadingImage = this.signerStore.uploadingImage;
  readonly currentSignerDetails = this.signerStore.currentSignerDetails;
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
      if (!this._visible) return;
      if (this.subsidiary?.subsidiaryPublicId) {
        this.signerStore.getSignerBySubsidiary(this.subsidiary.subsidiaryPublicId);
      }
    });
    
    effect(() => {
      const details = this.currentSignerDetails();
      if (details && this.visible) {
        if (details.responsibleEmployeeDocumentNumber) {
          this.signerForm.patchValue({ 
            employeeDocumentNumber: details.responsibleEmployeeDocumentNumber
          });
          this.employeeSearchStore.searchByDocumentNumber(details.responsibleEmployeeDocumentNumber);
        }
        this.signerForm.patchValue({ 
          responsiblePosition: details.responsiblePosition || '', 
          notes: details.notes || '' 
        });
        if (details.signatureImageUrl) {
          this.signerStore.setSignatureImageUrl(details.signatureImageUrl);
        }
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

  hideModal(): void {
    this.onHide.emit();
  }

  private initializeOnOpen(): void {
    this.signerForm.reset({ employeeDocumentNumber: '', responsiblePosition: '', notes: '' });
    this.signerForm.markAsPristine();
    this.employeeSearchStore.clearSearch();
  }

  private cleanupOnClose(): void {
    this.signerForm.reset({ employeeDocumentNumber: '', responsiblePosition: '', notes: '' });
    this.signerForm.markAsPristine();
    this.employeeSearchStore.clearSearch();
    this.signerStore.clearSignatureImage();
    this.clearSelectedImage();
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
    const fileInput = document.getElementById('signatureImageInputUpdate') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  clearExistingImage(): void {
    this.signerStore.clearSignatureImage();
    this.clearSelectedImage();
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

  updateSigner(): void {
    if (this.isSubmitDisabled()) return;
    
    this.isSubmitting.set(true);
    const formValue = this.signerForm.getRawValue();
    const details = this.currentSignerDetails();
    if (!details) {
      this.isSubmitting.set(false);
      return;
    }
    
    const imageFile = this.signatureImageFile();
    const existingImageUrl = this.signatureImageUrl();
    
    this.signerStore.updateSigner({
      publicId: details.publicId,
      request: {
        responsibleEmployeeDocumentNumber: formValue.employeeDocumentNumber!,
        responsiblePosition: formValue.responsiblePosition!,
        signatureImageUrl: existingImageUrl || null, // Mantener URL existente si no hay archivo nuevo
        notes: formValue.notes || null
      }
    });
  }
}