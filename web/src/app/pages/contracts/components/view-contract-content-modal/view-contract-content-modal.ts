import { Component, EventEmitter, inject, Input, OnChanges, OnDestroy, Output, signal, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { ContractStore } from '@core/store/contracts.store';
import { ContractListDTO } from '@shared/types/contract';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-view-contract-content-modal',
  standalone: true,
  imports: [
    CommonModule,
    ModalTemplateComponent
  ],
  templateUrl: './view-contract-content-modal.html',
  styleUrls: ['./view-contract-content-modal.css']
})
export class ViewContractContentModalComponent implements OnChanges, OnDestroy {
  @Input() visible: boolean = false;
  @Input() contract: ContractListDTO | null = null;
  @Output() onHide = new EventEmitter<void>();

  private contractStore = inject(ContractStore);
  private sanitizer = inject(DomSanitizer);
  private destroy$ = new Subject<void>();

  pdfPreviewUrl = signal<SafeResourceUrl | null>(null);
  loading = signal(false);

  ngOnChanges(changes: SimpleChanges): void {
    if (this.visible && this.contract) {
      this.loadContractPdf();
    }
  }

  ngOnDestroy(): void {
    const currentUrl = this.pdfPreviewUrl();
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
    const currentUrl = this.pdfPreviewUrl();
    if (currentUrl) {
      const urlString = currentUrl.toString();
      if (urlString.startsWith('blob:')) {
        this.contractStore.revokeBlobUrl(urlString);
      }
    }
    this.onHide.emit();
    this.pdfPreviewUrl.set(null);
  }

  private loadContractPdf() {
    if (!this.contract) {
      return;
    }

    this.loading.set(true);
    
    this.contractStore.getContractDetails(this.contract.publicId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (response.success && response.data && response.data.imageUrls && response.data.imageUrls.length > 0) {
            const pdfUrl = response.data.imageUrls[0].url;
            
            this.contractStore.getAuthenticatedFileBlobUrl(pdfUrl)
              .pipe(takeUntil(this.destroy$))
              .subscribe({
                next: (blobUrl) => {
                  if (blobUrl) {
                    const safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(blobUrl);
                    this.pdfPreviewUrl.set(safeUrl);
                  } else {
                    this.pdfPreviewUrl.set(null);
                  }
                  this.loading.set(false);
                },
                error: (err) => {
                  console.error('Error al cargar PDF autenticado:', err);
                  this.pdfPreviewUrl.set(null);
                  this.loading.set(false);
                }
              });
          } else {
            this.pdfPreviewUrl.set(null);
            this.loading.set(false);
          }
        },
        error: (err) => {
          console.error('Error cargando detalles del contrato:', err);
          this.pdfPreviewUrl.set(null);
          this.loading.set(false);
        }
      });
  }
}