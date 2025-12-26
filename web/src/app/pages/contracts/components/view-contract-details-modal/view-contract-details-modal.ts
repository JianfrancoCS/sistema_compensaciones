import { Component, EventEmitter, inject, Input, OnChanges, OnDestroy, Output, signal, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { ContractStore } from '@core/store/contracts.store';
import { ContractListDTO, ContractDetailsDTO, ContractImageDTO } from '@shared/types/contract';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-view-contract-details-modal',
  standalone: true,
  imports: [
    CommonModule,
    ModalTemplateComponent
  ],
  templateUrl: './view-contract-details-modal.html',
  styleUrls: ['./view-contract-details-modal.css']
})
export class ViewContractDetailsModalComponent implements OnChanges, OnDestroy {
  @Input() visible: boolean = false;
  @Input() contract: ContractListDTO | null = null;
  @Output() onHide = new EventEmitter<void>();

  private contractStore = inject(ContractStore);
  private sanitizer = inject(DomSanitizer);
  private destroy$ = new Subject<void>();

  contractDetails = signal<ContractDetailsDTO | null>(null);
  loading = signal(false);
  selectedPdfUrl = signal<SafeResourceUrl | null>(null);
  selectedPdfIndex = signal(0);
  loadingPdf = signal(false);

  ngOnChanges(changes: SimpleChanges): void {
    if (this.visible && this.contract) {
      this.loadContractDetails();
    }
  }

  ngOnDestroy(): void {
    const currentUrl = this.selectedPdfUrl();
    if (currentUrl) {
      const urlString = currentUrl.toString();
      if (urlString.startsWith('blob:')) {
        this.contractStore.revokeBlobUrl(urlString);
      }
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  hideModal() {
    const currentUrl = this.selectedPdfUrl();
    if (currentUrl) {
      const urlString = currentUrl.toString();
      if (urlString.startsWith('blob:')) {
        this.contractStore.revokeBlobUrl(urlString);
      }
    }
    this.onHide.emit();
    this.contractDetails.set(null);
    this.selectedPdfUrl.set(null);
    this.selectedPdfIndex.set(0);
  }

  private loadContractDetails() {
    if (!this.contract) {
      return;
    }

    this.loading.set(true);
    this.contractStore.getContractDetails(this.contract.publicId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.contractDetails.set(response.data);
          if (response.data.imageUrls && response.data.imageUrls.length > 0) {
            this.selectPdf(0);
          }
        }
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error cargando detalles del contrato:', err);
        this.loading.set(false);
      }
    });
  }

  selectPdf(index: number) {
    const details = this.contractDetails();
    if (details && details.imageUrls && details.imageUrls[index]) {
      const pdfUrl = details.imageUrls[index].url;
      
      const currentUrl = this.selectedPdfUrl();
      if (currentUrl) {
        const urlString = currentUrl.toString();
        if (urlString.startsWith('blob:')) {
          this.contractStore.revokeBlobUrl(urlString);
        }
      }
      
      this.loadingPdf.set(true);
      this.selectedPdfIndex.set(index);
      
      this.contractStore.getAuthenticatedFileBlobUrl(pdfUrl)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (blobUrl) => {
            if (blobUrl) {
              const safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(blobUrl);
              this.selectedPdfUrl.set(safeUrl);
            } else {
              this.selectedPdfUrl.set(null);
            }
            this.loadingPdf.set(false);
          },
          error: (err) => {
            console.error('Error al cargar PDF autenticado:', err);
            this.selectedPdfUrl.set(null);
            this.loadingPdf.set(false);
          }
        });
    }
  }

  getPdfFileName(url: string): string {
    const parts = url.split('/');
    const fileNameWithExtension = parts[parts.length - 1];
    return fileNameWithExtension.split('.')[0] || `Documento ${this.selectedPdfIndex() + 1}`;
  }
}