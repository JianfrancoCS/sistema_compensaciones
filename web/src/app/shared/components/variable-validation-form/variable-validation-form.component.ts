import { Component, Input, Output, EventEmitter, OnInit, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { CheckboxModule } from 'primeng/checkbox';
import { FloatLabelModule } from 'primeng/floatlabel';
import { TextareaModule } from 'primeng/textarea';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { VariableStore } from '@core/store/variables.store';
import { ValidationMethodDTO, VariableMethodRequest, VariableValidationRequest, VariableValidationDTO } from '@shared/types/variable';

interface ValidationMethodForm {
  methodPublicId: string;
  methodName: string;
  methodDescription: string;
  requiresValue: boolean;
  methodType: string;
  value: string;
  executionOrder: number;
  expanded: boolean;
}

@Component({
  selector: 'app-variable-validation-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InputTextModule,
    SelectModule,
    CheckboxModule,
    FloatLabelModule,
    TextareaModule,
    ButtonModule,
    TagModule,
    TooltipModule
  ],
  templateUrl: './variable-validation-form.component.html',
  styleUrl: './variable-validation-form.component.css'
})
export class VariableValidationFormComponent implements OnInit {
  @Input() variableId: string = '';

  @Input() set initialValidation(value: VariableValidationRequest | undefined) {
    console.log('🔄 SET initialValidation:', value);
    this._initialValidation = value;
    if (value && this.availableMethods().length > 0) {
      this.loadInitialValidation();
    }
  }
  get initialValidation() { return this._initialValidation; }
  private _initialValidation?: VariableValidationRequest;

  @Input() set fullValidationData(value: VariableValidationDTO | null | undefined) {
    console.log('🔄 SET fullValidationData:', value);
    this._fullValidationData = value;
    if (value) {
      this.loadFullValidationData();
    }
  }
  get fullValidationData() { return this._fullValidationData; }
  private _fullValidationData?: VariableValidationDTO | null;

  @Output() validationChange = new EventEmitter<VariableValidationRequest>();
  @Output() previewChange = new EventEmitter<{regex: string, errorMessage: string}>();

  private readonly variableStore = inject(VariableStore);

  selectedMethods: ValidationMethodForm[] = [];
  errorMessage: string = '';
  previewRegex: string = '';

  availableMethods = this.variableStore.validationMethods;
  loading = this.variableStore.validationLoading;

  private methodsLoadedEffect = effect(() => {
    const methods = this.availableMethods();
    if (methods.length > 0 && this.initialValidation && this.selectedMethods.length === 0) {
      console.log('🔄 Métodos disponibles cargados, reprocessando validación inicial...');
      this.loadInitialValidation();
    }
  });

  ngOnInit() {
    this.variableStore.loadValidationMethods();
    if (this.initialValidation) {
      this.loadInitialValidation();
    }
  }

  private loadFullValidationData() {
    if (!this.fullValidationData) return;

    console.log('🚀 loadFullValidationData() ejecutándose...');
    console.log('   - fullValidationData:', this.fullValidationData);

    this.errorMessage = this.fullValidationData.errorMessage || 'Formato inválido';
    this.selectedMethods = this.fullValidationData.methods.map(method => ({
      methodPublicId: method.methodPublicId,
      methodName: method.methodName,
      methodDescription: method.methodDescription,
      requiresValue: method.requiresValue,
      methodType: method.methodType,
      value: method.value || '',
      executionOrder: method.executionOrder,
      expanded: false
    }));

    console.log('   - selectedMethods configurados:', this.selectedMethods);
    console.log('   - errorMessage configurado:', this.errorMessage);
    this.sortMethodsByOrder();
    this.emitValidationChange();
  }

  private loadInitialValidation() {
    if (!this.initialValidation) return;

    console.log('🔧 loadInitialValidation() ejecutándose...');
    console.log('   - initialValidation:', this.initialValidation);
    console.log('   - métodos disponibles:', this.availableMethods().length);

    this.errorMessage = this.initialValidation.errorMessage || 'Formato inválido';
    this.selectedMethods = this.initialValidation.methods.map(method => {
      const foundMethod = this.findMethodById(method.methodPublicId);
      console.log(`   - Buscando método ${method.methodPublicId}:`, foundMethod ? 'ENCONTRADO' : 'NO ENCONTRADO');

      return {
        methodPublicId: method.methodPublicId,
        methodName: foundMethod?.name || '',
        methodDescription: foundMethod?.description || '',
        requiresValue: foundMethod?.requiresValue || false,
        methodType: foundMethod?.methodType || '',
        value: method.value || '',
        executionOrder: method.executionOrder,
        expanded: false
      };
    });

    console.log('   - selectedMethods configurados:', this.selectedMethods);
    this.sortMethodsByOrder();
    this.emitValidationChange();
  }

  private findMethodById(publicId: string): ValidationMethodDTO | null {
    return this.availableMethods().find(m => m.publicId === publicId) || null;
  }

  addValidationMethod() {
    const newMethod: ValidationMethodForm = {
      methodPublicId: '',
      methodName: '',
      methodDescription: '',
      requiresValue: false,
      methodType: '',
      value: '',
      executionOrder: this.selectedMethods.length + 1,
      expanded: true
    };

    this.selectedMethods.push(newMethod);
  }

  removeMethod(index: number) {
    this.selectedMethods.splice(index, 1);
    this.reorderMethods();
    this.emitValidationChange();
  }

  onMethodSelect(form: ValidationMethodForm, methodId: string) {
    const method = this.findMethodById(methodId);
    if (method) {
      form.methodPublicId = methodId;
      form.methodName = method.name;
      form.methodDescription = method.description || '';
      form.requiresValue = method.requiresValue;
      form.methodType = method.methodType;
      form.value = '';
    }
    this.emitValidationChange();
  }

  onMethodValueChange() {
    this.emitValidationChange();
  }

  onErrorMessageChange() {
    this.emitValidationChange();
  }

  moveMethodUp(index: number) {
    if (index > 0) {
      const temp = this.selectedMethods[index];
      this.selectedMethods[index] = this.selectedMethods[index - 1];
      this.selectedMethods[index - 1] = temp;
      this.reorderMethods();
      this.emitValidationChange();
    }
  }

  moveMethodDown(index: number) {
    if (index < this.selectedMethods.length - 1) {
      const temp = this.selectedMethods[index];
      this.selectedMethods[index] = this.selectedMethods[index + 1];
      this.selectedMethods[index + 1] = temp;
      this.reorderMethods();
      this.emitValidationChange();
    }
  }

  toggleMethodExpansion(index: number) {
    this.selectedMethods[index].expanded = !this.selectedMethods[index].expanded;
  }

  private reorderMethods() {
    this.selectedMethods.forEach((method, index) => {
      method.executionOrder = index + 1;
    });
  }

  private sortMethodsByOrder() {
    this.selectedMethods.sort((a, b) => a.executionOrder - b.executionOrder);
  }

  private emitValidationChange() {
    const validMethods = this.selectedMethods.filter(m =>
      m.methodPublicId && m.methodName &&
      (!m.requiresValue || (m.requiresValue && m.value?.trim()))
    );

    const validation: VariableValidationRequest = {
      errorMessage: this.errorMessage || '',
      methods: validMethods.map(m => {
        const methodData = {
          methodPublicId: m.methodPublicId,
          value: m.requiresValue ? (m.value?.trim() || null) : null,
          executionOrder: m.executionOrder
        };

        console.log(`🔧 Método ${m.methodName}:`, methodData);
        console.log(`   - Requiere valor: ${m.requiresValue}`);
        console.log(`   - Valor original: "${m.value}"`);
        console.log(`   - Valor procesado: ${methodData.value}`);

        return methodData;
      })
    };

    console.log('🔧 Validación final emitida (puede ser array vacío):', validation);
    this.validationChange.emit(validation);
    this.generatePreview(validation);
  }

  private generatePreview(validation: VariableValidationRequest) {
    const regexParts: string[] = [];

    validation.methods.forEach(method => {
      const methodDef = this.findMethodById(method.methodPublicId);
      if (methodDef) {
        switch (methodDef.code) {
          case 'NUMBERS_ONLY':
            regexParts.push('\\d');
            break;
          case 'LETTERS_ONLY':
            regexParts.push('[a-zA-Z]');
            break;
          case 'EXACT_LENGTH':
            if (method.value) {
              regexParts.push(`{${method.value}}`);
            }
            break;
          case 'LENGTH_RANGE':
            if (method.value) {
              const [min, max] = method.value.split(',');
              regexParts.push(`{${min},${max}}`);
            }
            break;
        }
      }
    });

    const previewRegex = regexParts.length > 0 ? `^[${regexParts.join('')}]+$` : '';

    this.previewChange.emit({
      regex: previewRegex,
      errorMessage: validation.errorMessage
    });
  }

  getAvailableMethodsForSelect(): any[] {
    return this.availableMethods().map(method => ({
      label: method.name,
      value: method.publicId,
      description: method.description
    }));
  }

  isFormValid(): boolean {
    if (this.selectedMethods.length === 0) {
      return true;
    }

    const hasValidMethods = this.selectedMethods.every(m =>
      m.methodPublicId && m.methodName &&
      (!m.requiresValue || (m.requiresValue && m.value?.trim()))
    );

    return hasValidMethods && this.errorMessage?.trim()?.length > 0;
  }

  clearValidation() {
    this.selectedMethods = [];
    this.errorMessage = '';
    this.emitValidationChange();
  }

  trackByIndex(index: number): number {
    return index;
  }
}