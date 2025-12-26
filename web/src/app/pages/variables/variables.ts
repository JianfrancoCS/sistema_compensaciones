import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CommonModule } from '@angular/common';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { VariableCreateModal } from './components/create-modal/create-modal';
import { VariableUpdateModal } from './components/update-modal/update-modal';
import { VariableValidationModalComponent } from '@shared/components/variable-validation-modal/variable-validation-modal.component';
import { VariableStore, VariableListDTO } from '@core/store/variables.store';

@Component({
  selector: 'app-variables',
  templateUrl: './variables.html',
  styleUrls: ['./variables.css'],
  standalone: true,
  imports: [TableModule, ButtonModule, InputTextModule, CommonModule, IconFieldModule, InputIconModule, ToastModule, TooltipModule, ConfirmDialogModule, VariableCreateModal, VariableUpdateModal, VariableValidationModalComponent],
  providers: [ConfirmationService]
})
export class Variables implements OnInit {
  private variableStore = inject(VariableStore);
  private confirmationService = inject(ConfirmationService);

  public variables = this.variableStore.variables;
  public loading = this.variableStore.loading;
  public totalRecords = this.variableStore.totalElements;
  public isEmpty = this.variableStore.isEmpty;

  isCreateModalVisible = signal(false);
  isUpdateModalVisible = signal(false);
  isValidationModalVisible = signal(false);
  selectedVariable = signal<VariableListDTO | null>(null);
  validationModalMode = signal<'create' | 'edit' | 'create-with-validation'>('create');

  ngOnInit() {
    this.variableStore.resetFilters();
    this.variableStore.init();
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.variableStore.search(inputElement.value);
  }

  loadVariables(event: TableLazyLoadEvent): void {
    this.variableStore.onLazyLoad(event);
  }

  showCreateModal() {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal() {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(variable: VariableListDTO) {
    this.selectedVariable.set(variable);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal() {
    this.isUpdateModalVisible.set(false);
    this.selectedVariable.set(null);
  }

  confirmDelete(variable: VariableListDTO) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar la variable ${variable.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.variableStore.delete(variable.publicId);
      }
    });
  }


  showValidationModal(variable: VariableListDTO) {
    console.log('🎯 showValidationModal() ejecutándose...');
    console.log('   - variable completa:', variable);
    console.log('   - variable.validationMethodsCount:', variable.validationMethodsCount);

    try {
      this.selectedVariable.set(variable);

      const hasValidation = variable.validationMethodsCount && variable.validationMethodsCount > 0;
      console.log('   - hasValidation calculado:', hasValidation);

      const mode = hasValidation ? 'edit' : 'create';
      console.log('   - modo configurado:', mode);

      this.validationModalMode.set(mode);
      this.isValidationModalVisible.set(true);

      console.log('✅ Modal configurado para abrirse en modo:', mode);
    } catch (error) {
      console.error('❌ Error en showValidationModal:', error);
    }
  }


  showCreateWithValidationModal() {
    this.selectedVariable.set(null);
    this.validationModalMode.set('create-with-validation');
    this.isValidationModalVisible.set(true);
  }

  hideValidationModal() {
    console.log('🔒 hideValidationModal() ejecutándose...');
    this.isValidationModalVisible.set(false);
    this.selectedVariable.set(null);
  }

  onValidationSaved() {
    this.variableStore.refresh();
  }

  onVariableCreated() {
    this.variableStore.refresh();
  }

  onVariableUpdated() {
    this.variableStore.refresh();
  }

  preSelectedVariable = computed(() => {
    const variable = this.selectedVariable();
    const mode = this.validationModalMode();

    if (variable && (mode === 'create' || mode === 'edit')) {
      return {
        publicId: variable.publicId,
        code: variable.code,
        name: variable.name,
        defaultValue: variable.defaultValue || '',
        isRequired: true
      };
    }
    return null;
  });
}
