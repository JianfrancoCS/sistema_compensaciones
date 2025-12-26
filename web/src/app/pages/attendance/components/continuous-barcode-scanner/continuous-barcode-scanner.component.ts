import { Component, Input, Output, EventEmitter, OnDestroy, AfterViewInit, ElementRef, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import '../../../../lib/dynamsoft.config';
import { CaptureVisionRouter } from 'dynamsoft-capture-vision-router';
import { CameraEnhancer, CameraView } from 'dynamsoft-camera-enhancer';
import { MultiFrameResultCrossFilter } from 'dynamsoft-utility';

@Component({
  selector: 'app-continuous-barcode-scanner',
  standalone: true,
  imports: [CommonModule, ButtonModule],
  template: `
    <div class="flex flex-col items-center justify-center h-full">
      @if (!isScanning()) {
        <div class="text-center space-y-4">
          <i class="pi pi-qrcode text-6xl text-gray-400"></i>
          <p class="text-gray-600 text-sm">Presiona el botón para comenzar a escanear</p>
          <button
            pButton
            type="button"
            label="Iniciar Escáner"
            icon="pi pi-camera"
            class="p-button-primary"
            [disabled]="isInitializing()"
            [loading]="isInitializing()"
            (click)="startScanning()">
          </button>
        </div>
      }

      <div class="w-full h-full flex flex-col" [class.hidden]="!isScanning()">
        <div #cameraViewContainer class="flex-1 relative rounded-lg overflow-hidden border-2 border-gray-300"></div>
        @if (isScanning()) {
          <div class="mt-4 flex justify-center gap-2">
            <button
              pButton
              type="button"
              label="Detener Escáner"
              icon="pi pi-stop"
              class="p-button-danger p-button-sm"
              (click)="stopScanning()">
            </button>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
    }
  `]
})
export class ContinuousBarcodeScannerComponent implements AfterViewInit, OnDestroy {
  @ViewChild('cameraViewContainer') cameraViewContainer!: ElementRef<HTMLDivElement>;

  @Input() isActive = true;
  @Output() onBarcodeScanned = new EventEmitter<string>();

  isInitializing = signal(false);
  isScanning = signal(false);
  lastScannedCode = signal<string | null>(null);

  private cvRouter: CaptureVisionRouter | null = null;
  private cameraEnhancer: CameraEnhancer | null = null;
  private pCvRouter: Promise<CaptureVisionRouter> | null = null;
  private pCameraEnhancer: Promise<CameraEnhancer> | null = null;
  private isDestroyed = false;

  ngAfterViewInit(): void {
  }

  async startScanning(): Promise<void> {
    if (this.isScanning() || this.isInitializing()) {
      return;
    }

    this.isInitializing.set(true);

    try {
      const cameraView = await CameraView.createInstance();
      if (this.isDestroyed) return;

      this.cameraEnhancer = await (this.pCameraEnhancer = this.pCameraEnhancer || CameraEnhancer.createInstance(cameraView));
      if (this.isDestroyed) return;

      this.cameraViewContainer.nativeElement.appendChild(cameraView.getUIElement());

      this.cvRouter = await (this.pCvRouter = this.pCvRouter || CaptureVisionRouter.createInstance());
      if (this.isDestroyed) return;

      this.cvRouter.setInput(this.cameraEnhancer);

      this.cvRouter.addResultReceiver({
        onDecodedBarcodesReceived: (result) => {
          if (!result.barcodeResultItems?.length) return;

          for (let item of result.barcodeResultItems) {
            console.log('Barcode scanned:', item.text);
            this.lastScannedCode.set(item.text);
            this.onBarcodeScanned.emit(item.text);
          }
        }
      });

      const filter = new MultiFrameResultCrossFilter();
      filter.enableResultCrossVerification('barcode', true);
      filter.enableResultDeduplication('barcode', true);
      await this.cvRouter.addResultFilter(filter);
      if (this.isDestroyed) return;

      await this.cameraEnhancer.open();
      if (this.isDestroyed) return;

      await this.cvRouter.startCapturing('ReadSingleBarcode');
      if (this.isDestroyed) return;

      this.isScanning.set(true);
      this.isInitializing.set(false);
    } catch (error) {
      console.error('Error starting scanner:', error);
      this.isInitializing.set(false);
      this.isScanning.set(false);
    }
  }

  async stopScanning(): Promise<void> {
    if (!this.isScanning()) return;

    try {
      if (this.cvRouter) {
        await this.cvRouter.stopCapturing();
      }

      if (this.cameraEnhancer) {
        await this.cameraEnhancer.close();
      }

      if (this.cameraViewContainer?.nativeElement) {
        this.cameraViewContainer.nativeElement.innerHTML = '';
      }

      this.isScanning.set(false);
    } catch (error) {
      console.error('Error stopping scanner:', error);
    }
  }

  async ngOnDestroy(): Promise<void> {
    this.isDestroyed = true;

    await this.stopScanning();

    if (this.pCvRouter) {
      await this.pCvRouter;
      this.cvRouter?.dispose();
      this.cvRouter = null;
    }

    if (this.pCameraEnhancer) {
      await this.pCameraEnhancer;
      this.cameraEnhancer?.dispose();
      this.cameraEnhancer = null;
    }
  }
}