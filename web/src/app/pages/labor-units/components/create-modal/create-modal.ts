import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateLaborUnitRequest } from '@shared/types/labor-unit';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { LaborUnitStore } from '@core/store/labor-unit.store';
import { MessageModule } from 'primeng/message';
import {Textarea} from 'primeng/textarea';

@Component({
  selector: 'app-labor-unit-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule,
    Textarea
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class LaborUnitCreateModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(LaborUnitStore);

  private isCreating = signal(false);

  laborUnitForm = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]*$/)]],
    abbreviation: ['', [Validators.required]],
    description: ['', [Validators.required]]
  });

  constructor() {
    effect(() => {
      if (!this.isCreating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isCreating.set(false);
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.laborUnitForm.reset();
    this.store.clearError();
  }

  createLaborUnit() {
    if (this.laborUnitForm.valid) {
      const request: CreateLaborUnitRequest = {
        name: this.laborUnitForm.value.name!,
        abbreviation: this.laborUnitForm.value.abbreviation!,
        description: this.laborUnitForm.value.description!
      };
      this.isCreating.set(true);
      this.store.create(request);
    }
  }
}
