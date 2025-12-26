import { Component, Input, Output, EventEmitter, inject, OnChanges, SimpleChanges, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MessageModule } from 'primeng/message';

import { BatchDetailsDTO, UpdateBatchRequest } from '@shared/types/batches';
import { BatchStore } from '@core/store/batches.store';
import {ModalTemplateComponent} from '@shared/components/modal-template/modal-template';
@Component({
  selector: 'app-batch-update-modal',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ReactiveFormsModule,
    MessageModule,
    ModalTemplateComponent
  ],
  templateUrl: './batch-update-modal.component.html',
  styles: []
})
export class BatchUpdateModalComponent implements OnInit, OnChanges {
  @Input() visible: boolean = false;
  @Input() batch: BatchDetailsDTO | null = null;
  @Output() onHide = new EventEmitter<void>();

  store = inject(BatchStore);
  private fb = inject(FormBuilder);

  batchForm!: FormGroup;

  ngOnInit(): void {
    this.batchForm = this.fb.group({
      publicId: [null],
      name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9\s]+$/)]],
      hectareage: [0, [Validators.required, Validators.min(0)]],
      subsidiaryPublicId: ['', Validators.required]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['batch'] && this.batch && this.batchForm) {
      this.batchForm.patchValue(this.batch);
    }
  }

  onSubmit() {
    if (this.batchForm.valid && this.batchForm.value.publicId) {
      const { publicId, ...request } = this.batchForm.value;
      this.store.update({ publicId, request });
      this.resetForm();
    }
  }

  onModalHide() {
    this.resetForm();
    this.onHide.emit();
  }

  private resetForm() {
    this.batchForm.reset({
      publicId: null,
      name: '',
      hectareage: 0,
      subsidiaryPublicId: ''
    });
  }
}
