import { Injectable } from '@angular/core';
import { environment } from 'environments/environment';
import { BarcodeScanner, BarcodeScannerConfig, BarcodeResult } from 'dynamsoft-barcode-reader-bundle';
import { BehaviorSubject } from 'rxjs';

export type { BarcodeScannerConfig, BarcodeResult };

@Injectable({
  providedIn: 'root',
})
export class BarcodeScannerService {
  private barcodeScanner?: BarcodeScanner;
  private isInitialized = new BehaviorSubject<boolean>(false);

  public isInitialized$ = this.isInitialized.asObservable();

  async initialize(container: HTMLElement): Promise<void> {

    if (this.isInitialized.getValue()) {
      return;
    }

    try {
      console.log('BarcodeScannerService: Step 1 - creating config');
      const config: BarcodeScannerConfig = {
        license: environment.dynamsoft.license,
        container: container,
        uiPath: "https://cdn.jsdelivr.net/npm/dynamsoft-barcode-reader-bundle@11.0.6000/dist/",
        engineResourcePaths: {
          rootDirectory: "https://cdn.jsdelivr.net/npm/",
        },
      };

      console.log('BarcodeScannerService: Step 2 - config created.');
      console.log('BarcodeScannerService: License being used:', config.license);
      console.log('BarcodeScannerService: Container element:', config.container);

      console.log('BarcodeScannerService: Step 3 - instantiating BarcodeScanner...');
      this.barcodeScanner = new BarcodeScanner(config);
      console.log('BarcodeScannerService: Step 4 - BarcodeScanner instantiated successfully.');

      console.log('BarcodeScannerService: Step 5 - About to set isInitialized to true.');
      this.isInitialized.next(true);
      console.log('BarcodeScannerService: Step 6 - isInitialized set to true.');

    } catch (error) {
      console.error('BarcodeScannerService: Failed to initialize barcode scanner in catch block:', error);
      console.error('BarcodeScannerService: Full error details:', JSON.stringify(error, Object.getOwnPropertyNames(error)));
      this.isInitialized.next(false);
      throw error;
    }
  }

  async launch(): Promise<BarcodeResult[]> {
    console.log('BarcodeScannerService: launch called');
    if (!this.barcodeScanner) {
      console.error('BarcodeScannerService: Scanner not initialized before launching.');
      throw new Error('Scanner not initialized before launching.');
    }
    try {
      console.log('BarcodeScannerService: calling barcodeScanner.launch()');
      const result = await this.barcodeScanner.launch();
      console.log('BarcodeScannerService: barcodeScanner.launch() returned raw result:', result);

      console.log('BarcodeScannerService: Scanner used successfully, marking as uninitialized for re-use');
      this.barcodeScanner = undefined;
      this.isInitialized.next(false);

      return result.barcodeResults || [];
    } catch (e) {
      console.error('BarcodeScannerService: Error during launch:', e);
      if (e instanceof Error && e.message.includes('destroyed')) {
        console.log('BarcodeScannerService: Scanner was destroyed, marking as uninitialized');
        this.barcodeScanner = undefined;
        this.isInitialized.next(false);
      }
      throw e;
    }
  }

  async decodeImage(file: File): Promise<BarcodeResult[]> {
    console.log('BarcodeScannerService: decodeImage called');
    if (!this.barcodeScanner) {
      console.error('BarcodeScannerService: Scanner not initialized for image decoding.');
      throw new Error('Scanner not initialized for image decoding.');
    }
    try {
      console.log('BarcodeScannerService: calling barcodeScanner.decode()');
      const results = await this.barcodeScanner.decode(file);
      console.log('BarcodeScannerService: barcodeScanner.decode() returned raw results:', results);
      return results || [];
    } catch (e) {
      console.error('BarcodeScannerService: Error during decodeImage:', e);
      throw e;
    }
  }

  async dispose(): Promise<void> {
    console.log('BarcodeScannerService: dispose called');
    if (this.barcodeScanner) {
      console.log('BarcodeScannerService: Attempting to destroy barcodeScanner instance.');
      try {
        await this.barcodeScanner.destroy();
        console.log('BarcodeScannerService: barcodeScanner instance destroyed successfully.');
      } catch (e) {
        console.error('BarcodeScannerService: Error destroying barcode scanner:', e);
      } finally {
        this.barcodeScanner = undefined;
        this.isInitialized.next(false);
        console.log('BarcodeScannerService: isInitialized set to false after dispose.');
      }
    }
  }
}
