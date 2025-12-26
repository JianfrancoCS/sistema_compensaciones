import { Component, EventEmitter, Input, Output, inject, signal, OnChanges, SimpleChanges, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea'; // Corrected: TextareaModule
import { MessageService } from 'primeng/api';
import { VariableStore } from '../../../../core/store/variables.store';
import { UpdateVariableRequest, UpdateVariableWithValidationRequest, VariableListDTO, VariableMethodRequest } from '@shared/types/variable';

@Component({
  selector: 'app-variable-update-modal',
  template: `
    <p-dialog
      header="Editar Variable"
      [modal]="true"
      [visible]="visible"
      (onHide)="onHide()"
      [style]="{ width: '500px' }"
      [closable]="!isSubmitting()"
      [closeOnEscape]="!isSubmitting()">

      <div class="p-6" *ngIf="variable">
        <form [formGroup]="variableForm" (ngSubmit)="onSubmit()" class="space-y-4">
          <!-- Código -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Código <span class="text-red-500">*</span>
            </label>
            <input
              pInputText
              formControlName="code"
              placeholder="Ej: EMPLOYEE_SALARY"
              class="w-full uppercase"
              [class.ng-invalid]="variableForm.get('code')?.invalid && variableForm.get('code')?.touched"
              maxlength="50" />
            <div *ngIf="variableForm.get('code')?.invalid && variableForm.get('code')?.touched"
                 class="text-red-500 text-sm mt-1">
              <div *ngIf="variableForm.get('code')?.errors?.['required']">
                El código es requerido
              </div>
              <div *ngIf="variableForm.get('code')?.errors?.['maxlength']">
                El código no puede exceder 50 caracteres
              </div>
            </div>
          </div>

          <!-- Nombre -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Nombre <span class="text-red-500">*</span>
            </label>
            <input
              pInputText
              formControlName="name"
              placeholder="Ej: Salario del empleado"
              class="w-full"
              [class.ng-invalid]="variableForm.get('name')?.invalid && variableForm.get('name')?.touched"
              maxlength="100" />
            <div *ngIf="variableForm.get('name')?.invalid && variableForm.get('name')?.touched"
                 class="text-red-500 text-sm mt-1">
              <div *ngIf="variableForm.get('name')?.errors?.['required']">
                El nombre es requerido
              </div>
              <div *ngIf="variableForm.get('name')?.errors?.['maxlength']">
                El nombre no puede exceder 100 caracteres
              </div>
            </div>
          </div>

          <!-- Valor por Defecto -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Valor por Defecto
            </label>
            <textarea
              pInputTextarea
              formControlName="defaultValue"
              placeholder="Valor opcional que se usará por defecto"
              rows="3"
              class="w-full"
              maxlength="500">
            </textarea>
            <div class="text-gray-500 text-sm mt-1">
              Opcional. Máximo 500 caracteres.
            </div>
          </div>

          <!-- Información de fechas -->
          <div class="bg-gray-50 p-4 rounded-lg">
            <div class="text-sm text-gray-600 space-y-1">
              <div><strong>Creado:</strong> {{ variable.createdAt | date: 'dd/MM/yyyy HH:mm' }}</div>
              <div><strong>Actualizado:</strong> {{ variable.updatedAt | date: 'dd/MM/yyyy HH:mm' }}</div>
            </div>
          </div>

          <!-- Botones -->
          <div class="flex justify-end gap-3 pt-4">
            <button
              type="button"
              pButton
              label="Cancelar"
              class="p-button-text"
              (click)="onHide()"
              [disabled]="isSubmitting()">
            </button>
            <button
              type="submit"
              pButton
              label="Guardar Cambios"
              icon="pi pi-check"
              class="p-button-success"
              [loading]="isSubmitting()"
              [disabled]="variableForm.invalid || isSubmitting() || !hasChanges()">
            </button>
          </div>
        </form>
      </div>
    </p-dialog>
  `,
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DialogModule, ButtonModule, InputTextModule, TextareaModule] // Corrected: TextareaModule
})
export class VariableUpdateModal implements OnChanges {
  @Input() visible = false;
  @Input() variable: VariableListDTO | null = null;
  @Input() includeValidation = false;
  @Input() validationData: { methods: VariableMethodRequest[] } | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() variableUpdated = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private variableStore = inject(VariableStore);
  private messageService = inject(MessageService);

  isSubmitting = signal(false);
  private originalValues: any = null;
  private wasSubmitting = false;

  private operationResultEffect = effect(() => {
    const loading = this.variableStore.loading();
    const error = this.variableStore.error();

    if (this.wasSubmitting && !loading) {
      this.wasSubmitting = false;
      this.isSubmitting.set(false);

      if (!error) {
        this.variableUpdated.emit();
        this.onHide();
      }
    }

    if (loading && this.isSubmitting()) {
      this.wasSubmitting = true;
    }
  });

  variableForm: FormGroup = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(50)]],
    name: ['', [Validators.required, Validators.maxLength(100)]],
    defaultValue: ['', [Validators.maxLength(500)]]
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['variable'] && this.variable) {
      this.loadVariableData();
    }
  }

  private loadVariableData(): void {
    if (this.variable) {
      const formData = {
        code: this.variable.code,
        name: this.variable.name,
        defaultValue: this.variable.defaultValue || ''
      };

      this.variableForm.patchValue(formData);
      this.originalValues = { ...formData };
    }
  }

  hasChanges(): boolean {
    if (!this.originalValues) return false;

    const currentValues = {
      code: this.variableForm.value.code?.trim(),
      name: this.variableForm.value.name?.trim(),
      defaultValue: this.variableForm.value.defaultValue?.trim() || ''
    };

    return JSON.stringify(currentValues) !== JSON.stringify(this.originalValues);
  }

  onHide(): void {
    if (!this.isSubmitting()) {
      this.visibleChange.emit(false);
      this.resetForm();
    }
  }

  onSubmit(): void {
    if (this.variableForm.valid && !this.isSubmitting() && this.variable && this.hasChanges()) {
      this.isSubmitting.set(true);

      if (this.includeValidation) {
        const request: UpdateVariableWithValidationRequest = {
          code: this.variableForm.value.code.toUpperCase().trim(),
          name: this.variableForm.value.name.trim(),
          defaultValue: this.variableForm.value.defaultValue?.trim() || undefined,
          validation: this.validationData
        };

        this.variableStore.updateWithValidation({ publicId: this.variable.publicId, request });
      } else {
        const request: UpdateVariableRequest = {
          code: this.variableForm.value.code.toUpperCase().trim(),
          name: this.variableForm.value.name.trim(),
          defaultValue: this.variableForm.value.defaultValue?.trim() || undefined
        };

        this.variableStore.update({ publicId: this.variable.publicId, request });
      }
    }
  }

  private resetForm(): void {
    this.variableForm.reset();
    this.originalValues = null;
    this.isSubmitting.set(false);
  }
}
