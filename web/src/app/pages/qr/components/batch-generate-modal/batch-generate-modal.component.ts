import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { MessageModule } from 'primeng/message';
import { InputNumberModule } from 'primeng/inputnumber';
import { QrRollStore } from '@core/store/qr-roll.store';
import { BatchGenerateQrCodesRequest } from '@shared/types/qr-roll';

@Component({
  selector: 'app-batch-generate-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ModalTemplateComponent,
    MessageModule,
    InputNumberModule
  ],
  templateUrl: './batch-generate-modal.component.html',
  styleUrl: './batch-generate-modal.component.css'
})
export class BatchGenerateModalComponent {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected qrRollStore = inject(QrRollStore);

  private isGenerating = signal(false);

  batchForm = this.fb.group({
    rollsNeeded: [1, [Validators.required, Validators.min(1)]],
    codesPerRoll: [100, [Validators.required, Validators.min(1), Validators.max(1000)]]
  });

  constructor() {
    effect(() => {
      if (!this.isGenerating()) {
        return;
      }

      if (!this.qrRollStore.loading()) {
        if (!this.qrRollStore.error()) {
          this.hideModal();
        }
        this.isGenerating.set(false);
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.batchForm.reset({
      rollsNeeded: 1,
      codesPerRoll: 100
    });
    this.qrRollStore.clearError();
  }

  generateBatch() {
    if (this.batchForm.invalid) {
      this.batchForm.markAllAsTouched();
      return;
    }

    const request: BatchGenerateQrCodesRequest = {
      rollsNeeded: this.batchForm.value.rollsNeeded!,
      codesPerRoll: this.batchForm.value.codesPerRoll!
    };
    this.isGenerating.set(true);
    this.qrRollStore.batchGenerate(request);
  }
}