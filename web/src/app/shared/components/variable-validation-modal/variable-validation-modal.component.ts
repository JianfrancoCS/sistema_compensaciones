import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { effect } from '@angular/core';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { VariableSelectorComponent } from '../variable-selector/variable-selector.component';
import { VariableValidationFormComponent } from '../variable-validation-form/variable-validation-form.component';

import { VariableStore } from '@core/store/variables.store';
import { VariableSelectOption, VariableValidationRequest, CreateVariableWithValidationRequest, UpdateVariableWithValidationRequest } from '@shared/types/variable';

@Component({
  selector: 'app-variable-validation-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DialogModule,
    ButtonModule,
    ToastModule,
    VariableSelectorComponent,
    VariableValidationFormComponent
  ],
  providers: [],
  templateUrl: './variable-validation-modal.component.html',
  styleUrl: './variable-validation-modal.component.css'
})
export class VariableValidationModalComponent implements OnInit {
  @Input() set visible(value: boolean) {
    console.log('🔍 SET VISIBLE:', value, 'anterior:', this._visible);
    this._visible = value;
    if (value) {
      console.log('🚀 Modal abriéndose, llamando onModalOpen...');
      this.onModalOpen();
    } else {
      console.log('🚪 Modal cerrándose, limpiando estado...');
      this.reset();
    }
  }
  get visible() { return this._visible; }
  private _visible: boolean = false;

  @Input() set mode(value: 'create' | 'edit' | 'create-with-validation') {
    console.log('🔍 SET MODE:', value, 'anterior:', this._mode, 'variableId:', this.variableId);
    this._mode = value;
    if (value === 'edit' && this.variableId) {
      console.log('📋 SET MODE: llamando loadVariableValidation...');
      this.loadVariableValidation();
    }
  }
  get mode() { return this._mode; }
  private _mode: 'create' | 'edit' | 'create-with-validation' = 'create';

  @Input() set variableId(value: string | undefined) {
    console.log('🔍 SET VARIABLE ID:', value, 'anterior:', this._variableId);
    this._variableId = value;

    if (this.visible && this.mode === 'edit' && value && this.validationMethodsCount && this.validationMethodsCount > 0) {
      console.log('📋 SET VARIABLE ID: Modal ya abierto, cargando validaciones...');
      this.loadVariableValidation();
    }
  }
  get variableId() { return this._variableId; }
  private _variableId?: string;

  @Input() set validationMethodsCount(value: number | undefined) {
    console.log('🔍 SET VALIDATION METHODS COUNT:', value);
    this._validationMethodsCount = value;

    if (this.visible && this.mode === 'edit' && this.variableId && value && value > 0) {
      console.log('📋 SET VALIDATION METHODS COUNT: Modal ya abierto, cargando validaciones...');
      this.loadVariableValidation();
    }
  }
  get validationMethodsCount() { return this._validationMethodsCount; }
  private _validationMethodsCount?: number;

  @Input() set preSelectedVariable(variable: VariableSelectOption | null) {
    if (variable && variable !== this.selectedVariable) {
      console.log('🔍 SET PRE-SELECTED VARIABLE (changed):', variable);
      this.selectedVariable = variable;
      if (this.mode === 'edit' && variable.publicId && this.variableId !== variable.publicId) {
        this.variableId = variable.publicId;
        console.log('📋 PRE-SELECTED: llamando loadVariableValidation...');
        this.loadVariableValidation();
      } else if (this.mode !== 'edit') {
        this.variableId = variable.publicId;
      }
    }
  }
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() variableSaved = new EventEmitter<void>();

  readonly variableStore = inject(VariableStore);

  selectedVariable: VariableSelectOption | null = null;
  currentValidation: VariableValidationRequest | null = null;
  previewData = { regex: '', errorMessage: '' };

  loading = this.variableStore.validationLoading;
  saving = this.variableStore.validationLoading;

  private onModalOpen() {
    console.log('🎬 onModalOpen() ejecutándose...');
    console.log('   - modo:', this.mode);
    console.log('   - variableId:', this.variableId);
    console.log('   - validationMethodsCount:', this.validationMethodsCount);
    console.log('   - selectedVariable:', this.selectedVariable);

    this.variableStore.loadValidationMethods();

    if (this.mode === 'edit' && this.variableId && this.validationMethodsCount && this.validationMethodsCount > 0) {
      console.log('✅ CONDICIONES CUMPLIDAS - cargando validaciones inmediatamente...');
      this.loadVariableValidation();
    } else {
      console.log('❌ CONDICIONES NO CUMPLIDAS para carga inmediata:');
      console.log('   - modo es edit:', this.mode === 'edit');
      console.log('   - tiene variableId:', !!this.variableId);
      console.log('   - tiene validationMethodsCount:', !!this.validationMethodsCount);
      if (this.validationMethodsCount) {
        console.log('   - validationMethodsCount > 0:', this.validationMethodsCount > 0);
      }
      console.log('   - Se esperará a que se configuren los inputs...');
    }
  }

  private loadVariableValidation() {
    if (!this.variableId) return;

    console.log('🔄 Cargando validación para variable:', this.variableId);
    this.variableStore.loadVariableValidation(this.variableId);
  }

  private validationDataEffect = effect(() => {
    const data = this.variableStore.currentVariableValidation();
    if (data && data.publicId === this.variableId) {
      console.log('✅ Datos de validación cargados desde el store:', data);

      if (data.methods && data.methods.length > 0) {
        this.currentValidation = {
          errorMessage: data.errorMessage,
          methods: data.methods.map((method: any) => ({
            methodPublicId: method.methodPublicId,
            value: method.value,
            executionOrder: method.executionOrder
          }))
        };

        console.log('🔧 Validación precargada:', this.currentValidation);

        this.previewData = {
          regex: data.finalRegex,
          errorMessage: data.errorMessage
        };
      } else {
        this.currentValidation = {
          errorMessage: '',
          methods: []
        };
      }
    }
  });


  ngOnInit() {
    this.variableStore.loadValidationMethods();
  }

  onVariableSelected(variables: VariableSelectOption[]) {
    this.selectedVariable = variables.length > 0 ? variables[0] : null;
  }

  onValidationChange(validation: VariableValidationRequest) {
    this.currentValidation = validation;
  }

  onPreviewChange(preview: {regex: string, errorMessage: string}) {
    this.previewData = preview;
  }

  canProceedToValidation(): boolean {
    return this.selectedVariable !== null;
  }

  canSave(): boolean {
    return this.canProceedToValidation() && this.currentValidation !== null;
  }


  save() {
    if (!this.selectedVariable || !this.currentValidation) {
      return;
    }

    if (this.mode === 'create-with-validation') {
      const request: CreateVariableWithValidationRequest = {
        code: this.selectedVariable.code,
        name: this.selectedVariable.name,
        defaultValue: this.selectedVariable.defaultValue || undefined,
        validation: {
          methods: this.currentValidation.methods
        }
      };

      this.variableStore.createWithValidation(request);
    } else if (this.mode === 'edit' && this.variableId) {
      const request: UpdateVariableWithValidationRequest = {
        code: this.selectedVariable.code,
        name: this.selectedVariable.name,
        defaultValue: this.selectedVariable.defaultValue || undefined,
        validation: {
          methods: this.currentValidation.methods
        }
      };

      console.log('🔧 Datos que se envían al PUT with-validation:', request);
      console.log('   - variableId:', this.variableId);
      console.log('   - methods:', this.currentValidation.methods);

      this.variableStore.updateWithValidation({ publicId: this.variableId, request });
    } else {
      this.variableStore.associateValidationMethods({ variableId: this.selectedVariable.publicId, request: this.currentValidation });
    }
  }

  private wasSaving = false;

  private saveStatusEffect = effect(() => {
    const isUsingValidationStore = (this.mode === 'create' && !this.selectedVariable);

    const isLoading = isUsingValidationStore ?
      this.variableStore.validationLoading() :
      this.variableStore.loading();

    const error = isUsingValidationStore ?
      this.variableStore.validationError() :
      this.variableStore.error();

    console.log('🔍 Save status changed:', { isLoading, error, mode: this.mode, wasSaving: this.wasSaving });

    if (this.wasSaving && !isLoading) {
      this.wasSaving = false;

      if (!error) {
        this.variableSaved.emit();
        this.close();
      }
    }

    if (isLoading) {
      this.wasSaving = true;
    }
  });

  close() {
    console.log('🚪 close() ejecutándose...');
    this._visible = false;
    this.visibleChange.emit(false);
  }

  onDialogHide() {
    console.log('🚪 onDialogHide() ejecutándose (botón X presionado)...');
    this.close();
  }

  onVisibilityChange(visible: boolean) {
    console.log('🔍 onVisibilityChange() ejecutándose:', visible);
    if (!visible) {
      this.close();
    }
  }

  private reset() {
    console.log('🧹 Reseteando estado del modal...');

    this.selectedVariable = null;
    this.currentValidation = null;
    this.previewData = { regex: '', errorMessage: '' };
    this.wasSaving = false;

    this._variableId = undefined;
    this._validationMethodsCount = undefined;

    this.variableStore.clearCurrentValidation();
    this.variableStore.clearValidationError();

    console.log('✅ Estado del modal reseteado completamente');
  }

  getModalTitle(): string {
    if (this.mode === 'edit') {
      return 'Editar Validación de Variable';
    } else if (this.mode === 'create-with-validation') {
      return 'Crear Variable con Validación';
    }
    return 'Configurar Validación de Variable';
  }

  getCurrentStepTitle(): string {
    return 'Configurar Validación';
  }
}
