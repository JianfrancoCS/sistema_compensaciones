import { Component, inject, Input, Output, EventEmitter, OnInit, OnDestroy, ViewChild, ElementRef, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { BarcodeScannerStore } from '../../../core/store/barcode-scanner.store';

@Component({
  selector: 'app-qr-scanner',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    ToastModule
  ],
  templateUrl: './qr-scanner.component.html',
  styleUrl: './qr-scanner.component.css',
  providers: [MessageService]
})
export class QrScannerComponent implements OnInit, OnDestroy {
  containerClass = 'qr-scanner-container';
  @Input() autoInitialize = true;
  @Input() showControls = true;
  @Input() showUploadButton = false;

  @Output() onScanSuccess = new EventEmitter<string>();
  @Output() onScanError = new EventEmitter<string>();
  @Output() onInitializationError = new EventEmitter<string>();

  @ViewChild('scannerContainer', { static: true }) scannerContainer!: ElementRef<HTMLDivElement>;

  private store = inject(BarcodeScannerStore);
  private messageService = inject(MessageService);

  readonly isReady = this.store.isReady;
  readonly isScanning = this.store.isScanning;
  readonly loading = this.store.loading;
  readonly error = this.store.error;
  readonly lastScannedCode = this.store.lastScannedCode;

  readonly initializationError = computed(() => this.loading() && this.error() ? this.error() : null);

  constructor() {
    effect(() => {
      const scannedCode = this.lastScannedCode();
      if (scannedCode) {
        this.onScanSuccess.emit(scannedCode);
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: `Código escaneado: ${scannedCode}`
        });
        this.store.clearLastScan();
      }
    });

    effect(() => {
      const error = this.error();
      if (error) {
        const isInitError = this.loading();
        if (isInitError) {
          this.onInitializationError.emit(error);
        }
        this.messageService.add({
          severity: 'error',
          summary: isInitError ? 'Error de Inicialización' : 'Error de Escaneo',
          detail: error
        });
      }
    });
  }

  ngOnInit(): void {
    console.log('QrScannerComponent: ngOnInit called. autoInitialize:', this.autoInitialize);
    if (this.autoInitialize) {
      this.initializeScanner();
    }
  }

  ngOnDestroy(): void {
    console.log('QrScannerComponent: ngOnDestroy called. Disposing scanner resources.');
    this.store.dispose();
  }

  initializeScanner(): void {
    console.log('QrScannerComponent: initializeScanner called');
    this.store.initialize(this.scannerContainer.nativeElement);
  }

  showScanner(): void {
    console.log('QrScannerComponent: showScanner called');
    this.store.showScanner(this.scannerContainer.nativeElement);
  }

  onFileUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (file) {
      this.store.decodeFromImage(file);
    }
  }

  clearError(): void {
    this.store.clearError();
  }

  retryInitialization(): void {
    this.store.clearError();
    this.initializeScanner();
  }
}
