import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { CreatePositionRequest } from '@shared/types/position';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { PositionStore } from '@core/store/position.store';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-position-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    FormsModule,
    CommonModule,
    ModalTemplateComponent,
    CheckboxModule,
    SelectModule,
    MessageModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class PositionCreateModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(PositionStore);

  private isCreating = signal(false);

  readonly areaOptions = this.store.areaSelectOptions;
  readonly managerPositionOptions = this.store.positionSelectOptions;

  filterByArea: boolean = false;

  positionForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z\s]*$/)]],
    areaPublicId: ['', Validators.required],
    salary: ['', [Validators.required, Validators.min(0.01)]],
    requiresManager: [false],
    requiredManagerPositionPublicId: [null],
    unique: [false]
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

    effect(() => {
      const requiresManager = this.positionForm.get('requiresManager')?.value;
      const requiredManagerPositionPublicIdControl = this.positionForm.get('requiredManagerPositionPublicId');

      if (requiresManager) {
        requiredManagerPositionPublicIdControl?.addValidators(Validators.required);
      } else {
        requiredManagerPositionPublicIdControl?.removeValidators(Validators.required);
        requiredManagerPositionPublicIdControl?.patchValue(null);
      }
      requiredManagerPositionPublicIdControl?.updateValueAndValidity();
    });

    effect(() => {
      const areaPublicId = this.positionForm.get('areaPublicId')?.value;
      if (!this.visible) return;
      
      if (this.filterByArea && areaPublicId) {
        this.store.loadPositionSelectOptionsByArea(areaPublicId);
      } else if (!this.filterByArea) {
        this.store.loadPositionSelectOptionsByArea(undefined);
      }
    });
  }

  onFilterByAreaChange(): void {
    const areaPublicId = this.positionForm.get('areaPublicId')?.value;
    if (this.filterByArea && areaPublicId) {
      this.store.loadPositionSelectOptionsByArea(areaPublicId);
    } else {
      this.store.loadPositionSelectOptionsByArea(undefined);
    }
  }

  hideModal() {
    this.onHide.emit();
    this.positionForm.reset();
    this.filterByArea = false;
    this.store.clearError();
  }

  createPosition() {
    if (this.positionForm.invalid) {
      this.positionForm.markAllAsTouched();
      return;
    }

    const { name, areaPublicId, salary, requiresManager, requiredManagerPositionPublicId, unique } = this.positionForm.value;

    const request: CreatePositionRequest = {
      name: name!,
      areaPublicId: areaPublicId!,
      salary: parseFloat(salary as string),
      requiresManager: requiresManager!,
      requiredManagerPositionPublicId: requiredManagerPositionPublicId || null,
      unique: unique!
    };

    this.isCreating.set(true);
    this.store.create(request);
  }
}
