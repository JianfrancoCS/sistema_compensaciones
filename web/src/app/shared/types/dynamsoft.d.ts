declare module 'dynamsoft-barcode-reader-bundle' {

  export interface BarcodeResult {
    barcodeText: string;
    barcodeFormat: string;
    barcodeBytes: Uint8Array;
    localizationResult: any;
    exception: any;
    isDPM: boolean;
    isMirrored: boolean;
    angle: number;
    moduleSize: number;
    confidence: number;
    [key: string]: any;
  }

  export interface BarcodeScannerConfig {
    license?: string;
    container?: HTMLElement;
    uiPath?: string;
    engineResourcePaths?: {
      rootDirectory?: string;
      [key: string]: any;
    };
    [key: string]: any;
  }

  export class BarcodeScanner {
    constructor(config: BarcodeScannerConfig);

    on(eventName: 'onresult', callback: (results: BarcodeResult[]) => void): void;
    decode(source: string | HTMLImageElement | HTMLCanvasElement | HTMLVideoElement | Blob | File): Promise<BarcodeResult[]>;
    launch(): Promise<{ barcodeResults: BarcodeResult[] }>; // Updated to reflect the actual return type from the example
    show(): Promise<void>;
    hide(): Promise<void>;
    destroy(): Promise<void>;
  }
}
