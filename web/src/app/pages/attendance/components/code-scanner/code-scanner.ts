import { Component, inject, Output, EventEmitter, effect } from '@angular/core';
import { MessageService } from 'primeng/api';
import { BarcodeScannerStore } from '@core/store/barcode-scanner.store';

@Component({
  selector: 'app-code-scanner',
  standalone: true,
  imports: [],
  providers: [MessageService],
  template: `
    <div class="code-scanner-wrapper">
      <button
        type="button"
        class="p-button p-button-primary"
        (click)="showScanner()"
        [disabled]="!isReady() || isScanning() || loading()"
        [attr.aria-disabled]="!isReady() || isScanning() || loading()">
        <i class="pi pi-camera"></i>
        <span>{{ loading() ? 'Cargando...' : (!isReady() ? 'Inicializando...' : (isScanning() ? 'Escaneando...' : 'Escanear Código')) }}</span>
      </button>
    </div>
  `
})
export class CodeScannerComponent {
  private messageService = inject(MessageService);
  private scannerStore = inject(BarcodeScannerStore);

  readonly loading = this.scannerStore.loading;
  readonly error = this.scannerStore.error;
  readonly lastScannedCode = this.scannerStore.lastScannedCode;
  readonly isReady = this.scannerStore.isReady;
  readonly isScanning = this.scannerStore.isScanning;

  @Output() codeScanned = new EventEmitter<string>();
  @Output() scanError = new EventEmitter<string>();

  constructor() {
    effect(() => {
      const scannedCode = this.scannerStore.lastScannedCode();
      if (scannedCode) {
        this.messageService.add({
          severity: 'success',
          summary: 'Código escaneado',
          detail: `Documento: ${scannedCode}`
        });

        this.codeScanned.emit(scannedCode);
        this.scannerStore.clearLastScan();
      }
    });

    effect(() => {
      const error = this.scannerStore.error();
      if (error) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error de escáner',
          detail: error
        });

        this.scanError.emit(error);
      }
    });
  }

  showScanner(): void {
    if (this.isReady() && !this.isScanning() && !this.loading()) {
      const container = document.createElement('div');
      document.body.appendChild(container);
      this.scannerStore.showScanner(container);
    }
  }
}
