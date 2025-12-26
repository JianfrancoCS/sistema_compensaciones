import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState, withHooks } from '@ngrx/signals';
import { BarcodeScannerService, type BarcodeResult } from '../services/barcode-scanner.service';

export type { BarcodeResult };

interface BarcodeScannerState {
  isInitialized: boolean;
  isScanning: boolean;
  loading: boolean;
  error: string | null;
  lastScannedCode: string | null;
}

const initialState: BarcodeScannerState = {
  isInitialized: false,
  isScanning: false,
  loading: false,
  error: null,
  lastScannedCode: null,
};

export const BarcodeScannerStore = signalStore(
  { providedIn: 'root' },
  withState<BarcodeScannerState>(initialState),

  withProps(() => ({
    _barcodeScannerService: inject(BarcodeScannerService)
  })),

  withComputed((store) => ({
    isReady: computed(() => !store.loading() && !store.isScanning() && !store.error()),
  })),

  withMethods((store) => ({
    async initialize(container: HTMLElement) {
      console.log('BarcodeScannerStore: initialize called');
      if (store.isInitialized()) {
        console.log('BarcodeScannerStore: already initialized, returning');
        return;
      }
      patchState(store, { loading: true, error: null });
      try {
        console.log('BarcodeScannerStore: calling service.initialize');
        await store._barcodeScannerService.initialize(container);
      } catch (e) {
        const errorMessage = e instanceof Error ? e.message : 'Unknown error during initialization';
        console.error('BarcodeScannerStore: Error during initialization:', errorMessage);
        patchState(store, { isInitialized: false, loading: false, error: errorMessage });
      }
    },

    async showScanner(container?: HTMLElement) {
      console.log('BarcodeScannerStore: showScanner called');
      if (store.isScanning()) {
        console.error('BarcodeScannerStore: Already scanning.');
        return;
      }

      patchState(store, { isScanning: true, error: null, lastScannedCode: null });

      try {
        if (!store.isInitialized() && container) {
          console.log('BarcodeScannerStore: Scanner not ready, re-initializing...');
          await store._barcodeScannerService.initialize(container);
        }

        if (!store.isInitialized()) {
          throw new Error('Scanner could not be initialized');
        }

        console.log('BarcodeScannerStore: calling service.launch');
        const results = await store._barcodeScannerService.launch();
        const scannedCode = results.length > 0 ? results[0]['text'] : null;

        console.log('BarcodeScannerStore: launch returned, scannedCode:', scannedCode);
        patchState(store, {
          lastScannedCode: scannedCode,
          isScanning: false,
        });

      } catch (e) {
        const errorMessage = e instanceof Error ? e.message : 'Unknown error during scan';
        console.error('BarcodeScannerStore: Error during showScanner:', errorMessage);
        patchState(store, { isScanning: false, error: errorMessage });
      }
    },

    async decodeFromImage(file: File) {
      console.log('BarcodeScannerStore: decodeFromImage called');
      if (store.loading() || store.isScanning()) {
        const error = 'Scanner is busy.';
        console.error('BarcodeScannerStore:', error);
        patchState(store, { error });
        return;
      }
      patchState(store, { loading: true, error: null, lastScannedCode: null });
      try {
        console.log('BarcodeScannerStore: calling service.decodeImage');
        const results = await store._barcodeScannerService.decodeImage(file);
        const scannedCode = results.length > 0 ? results[0]['text'] : null;
        console.log('BarcodeScannerStore: decodeImage returned, scannedCode:', scannedCode);
        patchState(store, {
          loading: false,
          lastScannedCode: scannedCode
        });
      } catch (e) {
        const errorMessage = e instanceof Error ? e.message : 'Error al decodificar imagen';
        console.error('BarcodeScannerStore: Error during decodeImage:', errorMessage);
        patchState(store, { loading: false, error: errorMessage });
      }
    },

    dispose() {
      console.log('BarcodeScannerStore: dispose called');
      store._barcodeScannerService.dispose();
      patchState(store, initialState);
    },

    clearLastScan: () => {
      console.log('BarcodeScannerStore: clearLastScan called');
      patchState(store, { lastScannedCode: null });
    },
    clearError: () => {
      console.log('BarcodeScannerStore: clearError called');
      patchState(store, { error: null });
    },
  })),

  withHooks({
    onInit({ _barcodeScannerService, ...store }) {
      console.log('BarcodeScannerStore: onInit hook');
      _barcodeScannerService.isInitialized$.subscribe((isInitialized) => {
        console.log('BarcodeScannerStore: service.isInitialized$ changed to', isInitialized);
        patchState(store, { isInitialized, loading: false });
      });
    },
    onDestroy({ dispose }) {
      console.log('BarcodeScannerStore: onDestroy hook');
      dispose();
    }
  })
);
