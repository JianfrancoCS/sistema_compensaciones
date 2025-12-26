import { Component, EventEmitter, Input, Output, inject, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea'; // Corrected: TextareaModule
import { MessageService } from 'primeng/api';
import { VariableStore } from '../../../../core/store/variables.store';
import { CreateVariableRequest, CreateVariableWithValidationRequest, VariableMethodRequest } from '@shared/types/variable';

@Component({
  selector: 'app-variable-create-modal',
  template: `
    <p-dialog
      header="Crear Nueva Variable"
      [modal]="true"
      [visible]="visible"
      (onHide)="onHide()"
      [style]="{ width: '500px' }"
      [closable]="!isSubmitting()"
      [closeOnEscape]="!isSubmitting()">

      <div class="p-6">
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
              label="Crear Variable"
              icon="pi pi-check"
              class="p-button-success"
              [loading]="isSubmitting()"
              [disabled]="variableForm.invalid || isSubmitting()">
            </button>
          </div>
        </form>
      </div>
    </p-dialog>
  `,
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DialogModule, ButtonModule, InputTextModule, TextareaModule] // Corrected: TextareaModule
})
export class VariableCreateModal {
  @Input() visible = false;
  @Input() includeValidation = false;
  @Input() validationData: { methods: VariableMethodRequest[] } | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() variableCreated = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private variableStore = inject(VariableStore);
  private messageService = inject(MessageService);

  isSubmitting = signal(false);
  private wasSubmitting = false;

  private operationResultEffect = effect(() => {
    const loading = this.variableStore.loading();
    const error = this.variableStore.error();

    if (this.wasSubmitting && !loading) {
      this.wasSubmitting = false;
      this.isSubmitting.set(false);

      if (!error) {
        this.variableCreated.emit();
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

  onHide(): void {
    if (!this.isSubmitting()) {
      this.visibleChange.emit(false);
      this.resetForm();
    }
  }

  onSubmit(): void {
    if (this.variableForm.valid && !this.isSubmitting()) {
      this.isSubmitting.set(true);

      if (this.includeValidation && this.validationData) {
        const request: CreateVariableWithValidationRequest = {
          code: this.variableForm.value.code.toUpperCase().trim(),
          name: this.variableForm.value.name.trim(),
          defaultValue: this.variableForm.value.defaultValue?.trim() || undefined,
          validation: this.validationData
        };

        this.variableStore.createWithValidation(request);
      } else {
        const request: CreateVariableRequest = {
          code: this.variableForm.value.code.toUpperCase().trim(),
          name: this.variableForm.value.name.trim(),
          defaultValue: this.variableForm.value.defaultValue?.trim() || undefined
        };

        this.variableStore.create(request);
      }
    }
  }

  private resetForm(): void {
    this.variableForm.reset();
    this.isSubmitting.set(false);
  }
}
