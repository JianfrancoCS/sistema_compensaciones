import { Component, effect, EventEmitter, inject, Input, Output, signal, OnChanges, SimpleChanges } from '@angular/core';
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
import { UpdateLaborRequest, LaborListDTO } from '@shared/types/labor';
import { TextareaModule } from 'primeng/textarea';

@Component({
  selector: 'app-labor-update-modal',
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
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.component.css'
})
export class UpdateLaborModalComponent implements OnChanges {
  @Input() visible: boolean = false;
  @Input() labor: LaborListDTO | null = null;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected laborStore = inject(LaborStore);
  protected laborUnitStore = inject(LaborUnitStore);

  private isUpdating = signal(false);

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
      if (!this.isUpdating()) {
        return;
      }

      if (!this.laborStore.loading()) {
        if (!this.laborStore.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
      }
    });

    effect(() => {
      this.laborUnitStore.loadSelectOptions();
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.labor) {
      const laborUnit = this.laborUnitStore.selectOptions().find(
        unit => unit.name === this.labor!.laborUnitName
      );

      this.laborForm.patchValue({
        name: this.labor.name,
        description: this.labor.description,
        minTaskRequirement: this.labor.minTaskRequirement,
        laborUnitPublicId: laborUnit?.publicId || '',
        isPiecework: this.labor.isPiecework,
        basePrice: this.labor.basePrice
      });
    }
  }

  hideModal() {
    this.onHide.emit();
    this.laborForm.reset();
    this.laborStore.clearError();
    this.labor = null;
  }

  updateLabor() {
    if (this.laborForm.invalid || !this.labor) {
      this.laborForm.markAllAsTouched();
      return;
    }

    const request: UpdateLaborRequest = {
      name: this.laborForm.value.name!,
      description: this.laborForm.value.description || '',
      minTaskRequirement: this.laborForm.value.minTaskRequirement!,
      laborUnitPublicId: this.laborForm.value.laborUnitPublicId!,
      isPiecework: this.laborForm.value.isPiecework!,
      basePrice: this.laborForm.value.basePrice!
    };
    this.isUpdating.set(true);
    this.laborStore.update({ publicId: this.labor.publicId, request });
  }
}
