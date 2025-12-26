import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { MessageModule } from 'primeng/message';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { LaborStore } from '@core/store/labor.store';
import { LaborUnitStore } from '@core/store/labor-unit.store';
import { CreateLaborRequest } from '@shared/types/labor';
import { TextareaModule } from 'primeng/textarea';

@Component({
  selector: 'app-labor-create-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ModalTemplateComponent,
    MessageModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ToggleSwitchModule,
    TextareaModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.component.css'
})
export class CreateLaborModalComponent {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected laborStore = inject(LaborStore);
  protected laborUnitStore = inject(LaborUnitStore);

  private isCreating = signal(false);

  laborForm = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]*$/)]],
    description: ['', [Validators.maxLength(500)]],
    minTaskRequirement: [0, [Validators.required, Validators.min(0)]],
    laborUnitPublicId: ['', [Validators.required]],
    isPiecework: [false, [Validators.required]],
    basePrice: [0, [Validators.required, Validators.min(0)]]
  });

  constructor() {
    effect(() => {
      if (!this.isCreating()) {
        return;
      }

      if (!this.laborStore.loading()) {
        if (!this.laborStore.error()) {
          this.hideModal();
        }
        this.isCreating.set(false);
      }
    });

    effect(() => {
      this.laborUnitStore.loadSelectOptions();
    });
  }

  hideModal() {
    this.onHide.emit();
    this.laborForm.reset();
    this.laborStore.clearError();
  }

  createLabor() {
    if (this.laborForm.valid) {
      const request: CreateLaborRequest = {
        name: this.laborForm.value.name!,
        description: this.laborForm.value.description || '',
        minTaskRequirement: this.laborForm.value.minTaskRequirement!,
        laborUnitPublicId: this.laborForm.value.laborUnitPublicId!,
        isPiecework: this.laborForm.value.isPiecework!,
        basePrice: this.laborForm.value.basePrice!
      };
      this.isCreating.set(true);
      this.laborStore.create(request);
    }
  }
}
