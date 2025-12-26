import { Component, EventEmitter, inject, Input, Output, OnChanges, SimpleChanges, signal, effect } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LaborUnitStore, LaborUnitListDTO, UpdateLaborUnitRequest } from '@core/store/labor-unit.store';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { MessageModule } from 'primeng/message';
import {Textarea} from 'primeng/textarea';

@Component({
  selector: 'app-labor-unit-update-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule,
    Textarea
  ],
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.css'
})
export class LaborUnitUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() laborUnit!: LaborUnitListDTO;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(LaborUnitStore);

  private isUpdating = signal(false);

  laborUnitForm = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]*$/)]],
    abbreviation: ['', [Validators.required]],
    description: ['', [Validators.required]]
  });

  constructor() {
    effect(() => {
      if (!this.isUpdating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.laborUnit) {
      this.laborUnitForm.patchValue({ name: this.laborUnit.name, description: this.laborUnit.description });
    }
  }

  hideModal() {
    this.onHide.emit();
    this.laborUnitForm.reset();
    this.store.clearError();
  }

  updateLaborUnit() {
    if (this.laborUnitForm.invalid) {
      this.laborUnitForm.markAllAsTouched();
      return;
    }

    const request: UpdateLaborUnitRequest = {
      name: this.laborUnitForm.value.name!,
      abbreviation: this.laborUnitForm.value.abbreviation!,
      description: this.laborUnitForm.value.description!,

    };

    this.isUpdating.set(true);
    this.store.update({ publicId: this.laborUnit.publicId, request });
  }
}
