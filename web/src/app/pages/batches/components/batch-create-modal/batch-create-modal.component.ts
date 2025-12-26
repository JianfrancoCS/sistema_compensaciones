import { Component, Input, Output, EventEmitter, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MessageModule } from 'primeng/message';

import { BatchStore } from '@core/store/batches.store';
import { CreateBatchRequest } from '@shared/types/batches';
import {ModalTemplateComponent} from '@shared/components/modal-template/modal-template';
@Component({
  selector: 'app-batch-create-modal',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ReactiveFormsModule, // Use ReactiveFormsModule
    MessageModule, // Add MessageModule
    ModalTemplateComponent // Add ModalTemplateComponent
  ],
  templateUrl: './batch-create-modal.component.html',
  styles: []
})
export class BatchCreateModalComponent implements OnInit {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  store = inject(BatchStore);
  private fb = inject(FormBuilder); // Inject FormBuilder

  batchForm!: FormGroup; // Declare FormGroup

  ngOnInit(): void {
    this.batchForm = this.fb.group({
      name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9\s]+$/)]],
      hectareage: [0, [Validators.required, Validators.min(0)]],
      subsidiaryPublicId: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.batchForm.valid) {
      const newBatch: CreateBatchRequest = this.batchForm.value;
      this.store.create(newBatch);
      this.resetForm();
    }
  }

  onModalHide() {
    this.resetForm();
    this.onHide.emit();
  }

  private resetForm() {
    this.batchForm.reset({
      name: '',
      hectareage: 0,
      subsidiaryPublicId: ''
    });
  }
}
