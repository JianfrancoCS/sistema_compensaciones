import { Component, EventEmitter, inject, Input, Output, signal, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { DatePickerModule } from 'primeng/datepicker';
import { EmployeeService, UpdateEmployeeRequest } from '../../../../core/services/employee.service';
import { LocationService } from '../../../../core/services/location.service';
import { SubsidiaryService } from '../../../../core/services/subsidiary.service';
import { PositionService } from '../../../../core/services/position.service';
import { ApiResult, SelectOption } from '../../../../core/models/api.model';

@Component({
  selector: 'app-employee-update-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ModalTemplateComponent,
    InputTextModule,
    ButtonModule,
    AutoCompleteModule,
    DatePickerModule
  ],
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.css'
})
export class EmployeeUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() employeePublicId!: string;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private employeeService = inject(EmployeeService);
  private messageService = inject(MessageService);
  private locationService = inject(LocationService);
  private subsidiaryService = inject(SubsidiaryService);
  private positionService = inject(PositionService);

  public employeeStates = signal<SelectOption[]>([]);
  public subsidiaryOptions = signal<SelectOption[]>([]);
  public positionOptions = signal<SelectOption[]>([]);
  public departments = signal<SelectOption[]>([]);
  public provinces = signal<SelectOption[]>([]);
  public districts = signal<SelectOption[]>([]);

  employeeForm = this.fb.group({
    documentNumber: [{ value: '', disabled: true }, [Validators.required, Validators.pattern(/^\d{8}$/)]],
    names: [{ value: '', disabled: true }, Validators.required],
    paternalLastname: [{ value: '', disabled: true }, Validators.required],
    maternalLastname: [{ value: '', disabled: true }, Validators.required],
    dob: [{ value: null as Date | null, disabled: true }, Validators.required],
    statePublicId: ['', Validators.required],
    subsidiaryPublicId: ['', Validators.required],
    positionPublicId: ['', Validators.required],
    departmentPublicId: ['', Validators.required],
    provincePublicId: ['', Validators.required],
    districtPublicId: ['', Validators.required],
    managerCode: ['']
  });

  constructor() {
    this.locationService.getDepartments().subscribe((res: ApiResult<SelectOption[]>) => res.success && this.departments.set(res.data));
    this.subsidiaryService.getSelectOptions().subscribe(response => {
      if (response.success) {
        this.subsidiaryOptions.set(response.data);
      }
    });
    this.positionService.getPositionsSelectOptions().subscribe((res: ApiResult<SelectOption[]>) => res.success && this.positionOptions.set(res.data));
    this.employeeService.getStatesForSelect().subscribe((res: ApiResult<SelectOption[]>) => res.success && this.employeeStates.set(res.data));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['employeePublicId'] && this.employeePublicId && this.visible) {
      this.loadEmployeeData();
    }
  }

  private loadEmployeeData(): void {
    this.employeeService.getEmployeeForEdit(this.employeePublicId).subscribe({
      next: (commandRes) => {
        if (commandRes.success && commandRes.data) {
          const employeeCmd = commandRes.data;
          this.employeeForm.patchValue({
            subsidiaryPublicId: employeeCmd.subsidiaryPublicId,
            positionPublicId: employeeCmd.positionPublicId,
            managerCode: employeeCmd.managerPublicId
          });

          this.employeeService.getEmployeeDetails(this.employeePublicId).subscribe(detailsRes => {
            if (detailsRes.success && detailsRes.data) {
              const employeeDetails = detailsRes.data;
              this.employeeForm.patchValue({
                documentNumber: employeeDetails.documentNumber, // Corrected property
                names: employeeDetails.names,
                paternalLastname: employeeDetails.paternalLastname,
                maternalLastname: employeeDetails.maternalLastname,
                dob: employeeDetails.dob ? new Date(employeeDetails.dob) : null,
              });
            }
          });
        }
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.message || 'Error al cargar datos del empleado.' });
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.employeeForm.reset();
    const fieldsToDisable = ['documentNumber', 'names', 'paternalLastname', 'maternalLastname', 'dob'];
    fieldsToDisable.forEach(field => this.employeeForm.get(field)?.disable());
    this.provinces.set([]);
    this.districts.set([]);
  }

  onDepartmentChange() {
    const departmentId = this.employeeForm.get('departmentPublicId')?.value;
    if (departmentId) {
      this.locationService.getProvincesByDepartmentId(departmentId).subscribe(response => {
        if (response.success) {
          this.provinces.set(response.data);
          this.employeeForm.get('provincePublicId')?.reset();
          this.employeeForm.get('districtPublicId')?.reset();
          this.districts.set([]);
        }
      });
    }
  }

  onProvinceChange() {
    const provinceId = this.employeeForm.get('provincePublicId')?.value;
    if (provinceId) {
      this.locationService.getDistrictsByProvinceId(provinceId).subscribe(response => {
        if (response.success) {
          this.districts.set(response.data);
          this.employeeForm.get('districtPublicId')?.reset();
        }
      });
    }
  }

  updateEmployee() {
    if (this.employeeForm.invalid) {
        this.messageService.add({ severity: 'warn', summary: 'Advertencia', detail: 'Formulario inválido. Revise los campos.' });
        return;
    }
    if (this.employeePublicId) {
      const formValue = this.employeeForm.getRawValue();
      const request: UpdateEmployeeRequest = {
        districtPublicId: formValue.districtPublicId!,
        subsidiaryPublicId: formValue.subsidiaryPublicId!,
        positionPublicId: formValue.positionPublicId!,
        statePublicId: formValue.statePublicId!,
        managerCode: formValue.managerCode || undefined
      };

      this.employeeService.update(this.employeePublicId, request).subscribe({
        next: (response) => {
          this.messageService.add({ severity: response.success ? 'success' : 'error', summary: response.success ? 'Éxito' : 'Error', detail: response.message });
          if (response.success) {
            this.hideModal();
          }
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'Ocurrió un error al actualizar el empleado' });
        }
      });
    }
  }
}
