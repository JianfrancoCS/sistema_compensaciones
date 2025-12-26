import { Component, Input, Output, EventEmitter, inject, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageModule } from 'primeng/message';

import { QrRollStore } from '@core/store/qr-roll.store';
import {ModalTemplateComponent} from '@shared/components/modal-template/modal-template';

@Component({
  selector: 'app-qr-generate-codes-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputNumberModule,
    MessageModule,
    ModalTemplateComponent
  ],
  templateUrl: './generate-codes-modal.component.html',
  styles: []
})
export class QrGenerateCodesModalComponent implements OnInit, OnChanges {
  @Input() visible: boolean = false;
  @Input() rollPublicId: string | null = null;
  @Output() onHide = new EventEmitter<void>();

  store = inject(QrRollStore);
  private fb = inject(FormBuilder);

  generateForm!: FormGroup;

  ngOnInit(): void {
    this.generateForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1), Validators.max(1000)]]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && changes['visible'].currentValue === true) {
      this.generateForm.patchValue({ quantity: 1 });
      this.generateForm.markAsUntouched();
      this.generateForm.markAsPristine();
    }
  }

  onSubmit(): void {
    if (this.generateForm.valid && this.rollPublicId) {
      const quantity = this.generateForm.value.quantity;
      this.store.generateCodes({ rollPublicId: this.rollPublicId, request: { quantity } }); // Corrected method name and request structure
      this.onModalHide();
    }
  }

  onModalHide(): void {
    this.resetForm();
    this.onHide.emit();
  }

  private resetForm() {
    this.generateForm.reset({ quantity: 1 });
  }
}
