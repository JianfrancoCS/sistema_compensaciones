import { Component, inject, OnInit, Output, EventEmitter, Input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CreateEmployeeRequest, UpdateEmployeeRequest } from '@shared/types/employee';
import { ApiResult, SelectOption } from '@core/models/api.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { SelectModule } from 'primeng/select'; // Corrected: Changed from DropdownModule to SelectModule
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { EmployeeStore } from '@core/store/employee.store'; // Import EmployeeStore
import { SubsidiaryStore } from '@core/store/subsidiary.store'; // Import SubsidiaryStore
import { PositionStore } from '@core/store/position.store';
import {ButtonModule} from 'primeng/button'; // Import PositionStore

@Component({
  selector: 'app-create-edit-employee',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    ToastModule,
    InputTextModule,
    SelectModule, // Corrected: Changed from DropdownModule to SelectModule
    ProgressSpinnerModule
  ],
  providers: [MessageService],
  templateUrl: './create-edit-employee.html',
  styleUrls: ['./create-edit-employee.css']
})
export class CreateEditEmployeeComponent implements OnInit {
  private employeeStore = inject(EmployeeStore); // Inject EmployeeStore
  private subsidiaryStore = inject(SubsidiaryStore); // Inject SubsidiaryStore
  private positionStore = inject(PositionStore); // Inject PositionStore
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  @Input() employeeId: string | null = null;
  @Output() onBack = new EventEmitter<boolean>();

  employeeForm!: FormGroup;
  isEditMode = computed(() => !!this.employeeId);

  subsidiaries = this.subsidiaryStore.selectOptions; // Get from SubsidiaryStore
  positions = this.positionStore.positionSelectOptions; // Get from PositionStore
  isLoading = computed(() => this.employeeStore.loading()); // Get loading state from EmployeeStore

  ngOnInit(): void {
    this.initForm();
    if (this.isEditMode()) {
      this.loadEmployeeForEdit();
    }
  }

  private initForm(): void {
    this.employeeForm = this.fb.group({
      documentNumber: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(8)]],
      subsidiaryPublicId: ['', Validators.required],
      positionPublicId: ['', Validators.required]
    });

    if (this.isEditMode()) {
      this.employeeForm.get('documentNumber')?.disable();
    }
  }

  private loadEmployeeForEdit(): void {
    if (!this.employeeId) return;

    this.employeeStore.getDetails(this.employeeId).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const employeeData = res.data;
          this.employeeForm.patchValue({
            documentNumber: employeeData.documentNumber,
            subsidiaryPublicId: employeeData.subsidiaryPublicId,
            positionPublicId: employeeData.positionPublicId
          });
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: res.message || 'No se pudo cargar el empleado para editar.' });
          this.goBack(false);
        }
      },
      error: (err) => {
        const errorMessage = err.error?.message || err.message || 'No se pudo cargar el empleado.';
        this.messageService.add({ severity: 'error', summary: 'Error de Conexión', detail: errorMessage });
        console.error(err);
        this.goBack(false);
      }
    });
  }

  saveEmployee(): void {
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      this.messageService.add({ severity: 'warn', summary: 'Formulario Inválido', detail: 'Por favor, complete todos los campos requeridos.' });
      return;
    }

    const formValue = this.employeeForm.getRawValue();

    if (this.isEditMode()) {
      this.employeeStore.update({ publicId: this.employeeId!, request: formValue as UpdateEmployeeRequest });
      this.messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Empleado actualizado correctamente' });
    } else {
      this.employeeStore.create(formValue as CreateEmployeeRequest);
      this.messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Empleado creado correctamente' });
    }
    this.goBack(true);
  }

  goBack(refresh: boolean = false): void {
    this.onBack.emit(refresh);
  }
}
