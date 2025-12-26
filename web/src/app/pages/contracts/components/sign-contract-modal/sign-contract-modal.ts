import { Component, EventEmitter, inject, Input, Output, signal, viewChild, ElementRef, effect, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { FileUploadModule } from 'primeng/fileupload';
import { ContractStore } from '@core/store/contracts.store';
import { ContractListDTO } from '@shared/types/contract';

@Component({
  selector: 'app-sign-contract-modal',
  standalone: true,
  imports: [
    CommonModule,
    DialogModule,
    ButtonModule,
    FileUploadModule
  ],
  templateUrl: './sign-contract-modal.html',
  styleUrls: ['./sign-contract-modal.css']
})
export class SignContractModalComponent implements OnInit, OnDestroy {
  @Input() visible: boolean = false;
  @Input() contract: ContractListDTO | null = null;
  @Output() onHide = new EventEmitter<void>();
  @Output() onSigned = new EventEmitter<void>();

  private contractStore = inject(ContractStore);

  canvasRef = viewChild<ElementRef<HTMLCanvasElement>>('signatureCanvas');
  
  signatureMode = signal<'upload' | 'draw'>('draw'); // 'upload' o 'draw'
  isDrawing = signal(false);
  hasSignature = signal(false);
  signing = signal(false);
  uploadedFile = signal<File | null>(null); // Archivo subido en modo upload
  
  private canvas: HTMLCanvasElement | null = null;
  private ctx: CanvasRenderingContext2D | null = null;
  private startX = 0;
  private startY = 0;

  ngOnInit() {
    if (this.canvasRef()) {
      this.initCanvas();
    }
  }

  ngOnDestroy() {
    this.clearCanvas();
  }

  effectCanvas = effect(() => {
    const canvasEl = this.canvasRef();
    if (canvasEl?.nativeElement) {
      this.initCanvas();
    }
  });

  initCanvas() {
    const canvasEl = this.canvasRef()?.nativeElement;
    if (!canvasEl) return;

    this.canvas = canvasEl;
    this.ctx = canvasEl.getContext('2d');
    
    if (!this.ctx) return;

    this.ctx.strokeStyle = '#000000';
    this.ctx.lineWidth = 2;
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';

    canvasEl.addEventListener('mousedown', this.onMouseDown.bind(this));
    canvasEl.addEventListener('mousemove', this.onMouseMove.bind(this));
    canvasEl.addEventListener('mouseup', this.onMouseUp.bind(this));
    canvasEl.addEventListener('mouseout', this.onMouseUp.bind(this));

    canvasEl.addEventListener('touchstart', this.onTouchStart.bind(this));
    canvasEl.addEventListener('touchmove', this.onTouchMove.bind(this));
    canvasEl.addEventListener('touchend', this.onTouchEnd.bind(this));
  }

  onMouseDown(e: MouseEvent) {
    if (!this.canvas || !this.ctx || this.signatureMode() !== 'draw') return;
    
    const rect = this.canvas.getBoundingClientRect();
    this.startX = e.clientX - rect.left;
    this.startY = e.clientY - rect.top;
    this.isDrawing.set(true);
    this.ctx.beginPath();
    this.ctx.moveTo(this.startX, this.startY);
  }

  onMouseMove(e: MouseEvent) {
    if (!this.isDrawing() || !this.canvas || !this.ctx || this.signatureMode() !== 'draw') return;
    
    const rect = this.canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    this.ctx.lineTo(x, y);
    this.ctx.stroke();
    this.hasSignature.set(true);
  }

  onMouseUp() {
    this.isDrawing.set(false);
  }

  onTouchStart(e: TouchEvent) {
    if (!this.canvas || !this.ctx || this.signatureMode() !== 'draw') return;
    
    e.preventDefault();
    const rect = this.canvas.getBoundingClientRect();
    const touch = e.touches[0];
    this.startX = touch.clientX - rect.left;
    this.startY = touch.clientY - rect.top;
    this.isDrawing.set(true);
    this.ctx.beginPath();
    this.ctx.moveTo(this.startX, this.startY);
  }

  onTouchMove(e: TouchEvent) {
    if (!this.isDrawing() || !this.canvas || !this.ctx || this.signatureMode() !== 'draw') return;
    
    e.preventDefault();
    const rect = this.canvas.getBoundingClientRect();
    const touch = e.touches[0];
    const x = touch.clientX - rect.left;
    const y = touch.clientY - rect.top;
    
    this.ctx.lineTo(x, y);
    this.ctx.stroke();
    this.hasSignature.set(true);
  }

  onTouchEnd() {
    this.isDrawing.set(false);
  }

  clearCanvas() {
    if (!this.canvas || !this.ctx) return;
    
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    this.hasSignature.set(false);
  }

  switchMode(mode: 'upload' | 'draw') {
    this.signatureMode.set(mode);
    if (mode === 'draw') {
      this.clearCanvas();
    }
  }

  onFileSelected(event: any) {
    const files = Array.from(event.files) as File[];
    if (files.length > 0) {
      const file = files[0];
      if (file.type.startsWith('image/') || file.type === 'application/pdf') {
        this.uploadedFile.set(file);
        this.hasSignature.set(true);
        this.contractStore.showToast('info', 'Archivo seleccionado', 'Haz clic en "Firmar Contrato" para continuar');
      } else {
        this.contractStore.showToast('warn', 'Formato inválido', 'Por favor selecciona una imagen (PNG, JPG) o PDF');
      }
    }
  }

  async signContract() {
    if (!this.contract) return;

    let signatureFile: File | null = null;

    if (this.signatureMode() === 'draw') {
      if (!this.hasSignature()) {
        this.contractStore.showToast('warn', 'Firma requerida', 'Por favor dibuja tu firma en el canvas');
        return;
      }

      signatureFile = await this.canvasToFile();
      if (!signatureFile) {
        this.contractStore.showToast('error', 'Error', 'No se pudo generar la imagen de la firma');
        return;
      }
    } else {
      const uploadedFile = this.uploadedFile();
      if (!uploadedFile) {
        this.contractStore.showToast('warn', 'Firma requerida', 'Por favor selecciona un archivo con la firma');
        return;
      }
      signatureFile = uploadedFile;
    }

    if (!signatureFile) {
      this.contractStore.showToast('error', 'Error', 'No se pudo obtener la firma');
      return;
    }

    this.signing.set(true);
    this.contractStore.signContract(this.contract.publicId, signatureFile).subscribe({
      next: (response) => {
        this.signing.set(false);
        if (response.success) {
          this.onSigned.emit();
          this.hideModal();
        }
      },
      error: (err) => {
        this.signing.set(false);
      }
    });
  }

  private async canvasToFile(): Promise<File | null> {
    if (!this.canvas) return null;

    return new Promise((resolve) => {
      this.canvas!.toBlob((blob) => {
        if (!blob) {
          resolve(null);
          return;
        }
        const file = new File([blob], 'signature.png', { type: 'image/png' });
        resolve(file);
      }, 'image/png');
    });
  }

  hideModal() {
    this.clearCanvas();
    this.uploadedFile.set(null);
    this.signatureMode.set('draw');
    this.onHide.emit();
  }
}

