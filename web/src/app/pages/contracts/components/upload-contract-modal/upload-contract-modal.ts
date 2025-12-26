import { Component, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { ContractStore } from '@core/store/contracts.store';
import { ContractListDTO, FileWithOrder } from '@shared/types/contract';
import { FileUploadHandlerEvent, FileUploadModule } from 'primeng/fileupload';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-upload-contract-modal',
  standalone: true,
  imports: [
    CommonModule,
    ModalTemplateComponent,
    FileUploadModule,
    ButtonModule,
    TooltipModule
  ],
  templateUrl: './upload-contract-modal.html',
  styleUrls: ['./upload-contract-modal.css']
})

export class UploadContractModalComponent {
  @Input() visible: boolean = false;
  @Input() contract: ContractListDTO | null = null;
  @Output() onHide = new EventEmitter<void>();

  private contractStore = inject(ContractStore);

  selectedFile = signal<File | null>(null);
  uploading = signal(false);

  hideModal() {
    this.onHide.emit();
    this.selectedFile.set(null);
  }

  onFileSelect(event: any) {
    const files = Array.from(event.files) as File[];
    if (files.length > 0) {
      this.selectedFile.set(files[0]); 
    }
  }

  uploadFile() {
    const file = this.selectedFile();
    if (!this.contract || !file) return;

    this.uploading.set(true);

    this.contractStore.uploadAndAttachFile(this.contract.publicId, file).subscribe({
      next: (response) => {
        if (response.success) {
          this.hideModal();
        }
        this.uploading.set(false);
      },
      error: () => {
        this.uploading.set(false);
      }
    });
  }
}
