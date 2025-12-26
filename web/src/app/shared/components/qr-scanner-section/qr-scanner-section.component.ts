import { Component, Input, Output, EventEmitter, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { QrScannerComponent } from '../qr-scanner/qr-scanner.component';

@Component({
  selector: 'app-qr-scanner-section',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    QrScannerComponent
  ],
  templateUrl: './qr-scanner-section.component.html',
  styleUrl: './qr-scanner-section.component.css'
})
export class QrScannerSectionComponent {
  @ViewChild(QrScannerComponent) qrScannerComponent!: QrScannerComponent;

  @Input() showControls = true;
  @Input() showUploadButton = false;
  @Input() autoInitialize = true;

  @Output() onScanSuccess = new EventEmitter<string>();
  @Output() onScanError = new EventEmitter<string>();

  isScanning = signal(false);

  getScannerButtonLabel(): string {
    if (!this.qrScannerComponent?.isReady()) {
      return 'Inicializando Escáner...';
    }
    if (this.qrScannerComponent?.isScanning()) {
      return 'Escaneando...';
    }
    return 'Escanear Código QR';
  }

  startScanning(): void {
    this.qrScannerComponent?.showScanner();
    this.isScanning.set(true);
  }

  onQrScanSuccess(scannedCode: string): void {
    this.onScanSuccess.emit(scannedCode);
    this.isScanning.set(false);
  }

  onQrScanError(error: string): void {
    this.onScanError.emit(error);
  }

  get scannerReady(): boolean {
    return this.qrScannerComponent?.isReady() || false;
  }

  get scannerIsScanning(): boolean {
    return this.qrScannerComponent?.isScanning() || false;
  }
}