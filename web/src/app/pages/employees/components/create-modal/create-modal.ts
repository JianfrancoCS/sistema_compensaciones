import { Component, EventEmitter, inject, Input, Output, signal, effect, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';
import { EmployeeStore } from '../../../../core/store/employee.store';
import { LocationStore } from '../../../../core/store/location.store';

@Component({
  selector: 'app-employee-create-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ModalTemplateComponent,
    InputTextModule,
    ButtonModule,
    AutoCompleteModule,
    DatePickerModule,
    SelectModule,
    MessageModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class EmployeeCreateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected employeeStore = inject(EmployeeStore);
  protected locationStore = inject(LocationStore);

  readonly subsidiaryOptions = this.employeeStore.subsidiarySelectOptions;
  readonly positionOptions = this.employeeStore.positionSelectOptions;
  readonly departments = this.locationStore.departments;
  readonly provinces = this.locationStore.provinces;
  readonly districts = this.locationStore.districts;
  readonly foundPerson = this.employeeStore.foundPerson;
  readonly searchingPerson = this.employeeStore.searchingPerson;

  readonly maxDate = new Date();

  employeeForm = this.fb.group({
    isNational: [true, Validators.required], // Por defecto nacional (DNI)
    documentNumber: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    birthdateForSearch: [{ value: null as Date | null, disabled: true }],
    names: [{ value: '', disabled: true }, Validators.required],
    paternalLastname: [{ value: '', disabled: true }, Validators.required],
    maternalLastname: [{ value: '', disabled: true }, Validators.required],
    dob: [{ value: null as Date | null, disabled: true }, Validators.required],
    subsidiaryPublicId: ['', Validators.required],
    positionPublicId: ['', Validators.required],
    departmentPublicId: [''],
    provincePublicId: [''],
    districtPublicId: [''],
    managerCode: ['']
  });

  private visibleSignal = signal(false);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']) {
      this.visibleSignal.set(this.visible);
      if (this.visible) {
        this.enablePersonFields(false);
      }
    }
  }

  private loadInitialData(): void {
    if (this.departments().length === 0) {
      this.locationStore.loadDepartments();
    }
    if (this.subsidiaryOptions().length === 0) {
      this.employeeStore.refresh();
    }
  }

  hideModal() {
    this.onHide.emit();
    this.employeeForm.reset({
      isNational: true
    });
    this.enablePersonFields(false);
    const birthdateControl = this.employeeForm.get('birthdateForSearch');
    if (this.employeeForm.get('isNational')?.value) {
      birthdateControl?.disable();
      birthdateControl?.setValue(null);
    }
    this.employeeStore.clearPersonSearch();
    this.locationStore.resetProvinces();
  }

  private isCreatingEmployee = signal(false);

  onDniLookup() {
    const docNumber = this.employeeForm.get('documentNumber')?.value;
    const isNational = this.employeeForm.get('isNational')?.value ?? true;
    const birthdate = this.employeeForm.get('birthdateForSearch')?.value as Date | null;
    
    if (docNumber && this.employeeForm.get('documentNumber')?.valid) {
      if (isNational && docNumber.length !== 8) {
        return;
      } else if (!isNational && docNumber.length !== 9) {
        return;
      }

      const birthdateStr = birthdate ? this.formatDateForApi(birthdate) : null;

      this.employeeStore.searchPersonByDocument({
        documentNumber: docNumber,
        isNational,
        birthdate: birthdateStr
      });
    }
  }

  private formatDateForApi(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private enablePersonFields(enable: boolean): void {
    const fields = ['names', 'paternalLastname', 'maternalLastname', 'dob'];
    fields.forEach(field => {
      const control = this.employeeForm.get(field);
      if (enable) {
        control?.enable();
        if (field === 'names' || field === 'paternalLastname' || field === 'maternalLastname' || field === 'dob') {
          control?.setValidators([Validators.required]);
          control?.updateValueAndValidity();
        }
      } else {
        control?.disable();
        control?.clearValidators();
        control?.updateValueAndValidity();
      }
    });
  }

  onDepartmentChange() {
    const departmentId = this.employeeForm.get('departmentPublicId')?.value;
    if (departmentId) {
      this.locationStore.loadProvinces(departmentId);
      this.employeeForm.get('provincePublicId')?.reset();
      this.employeeForm.get('districtPublicId')?.reset();
    }
  }

  onProvinceChange() {
    const provinceId = this.employeeForm.get('provincePublicId')?.value;
    if (provinceId) {
      this.locationStore.loadDistricts(provinceId);
      this.employeeForm.get('districtPublicId')?.reset();
    }
  }

  constructor() {
    this.enablePersonFields(false);

    effect(() => {
      if (this.visibleSignal()) {
        this.loadInitialData();
        this.enablePersonFields(false);
      }
    });

    effect(() => {
      const person = this.foundPerson();
      if (person) {
        this.employeeForm.patchValue({
          names: person.names,
          paternalLastname: person.paternalLastname,
          maternalLastname: person.maternalLastname,
          dob: person.dob ? new Date(person.dob) : null
        });
        this.enablePersonFields(false);
      } else if (this.employeeStore.personSearchError()) {
        this.enablePersonFields(true);
      }
    });

    effect(() => {
      if (this.isCreatingEmployee() && this.employeeStore.successMessage() && !this.employeeStore.loading()) {
        this.hideModal();
        this.isCreatingEmployee.set(false);
      }
    });

    this.employeeForm.get('isNational')?.valueChanges.subscribe(isNational => {
      const docControl = this.employeeForm.get('documentNumber');
      const birthdateControl = this.employeeForm.get('birthdateForSearch');
      
      if (docControl) {
        if (isNational) {
          docControl.setValidators([Validators.required, Validators.pattern(/^\d{8}$/)]);
          birthdateControl?.setValue(null);
          birthdateControl?.disable();
          birthdateControl?.clearValidators();
        } else {
          docControl.setValidators([Validators.required, Validators.pattern(/^\d{9}$/)]);
          birthdateControl?.enable();
          birthdateControl?.clearValidators();
        }
        docControl.updateValueAndValidity();
        birthdateControl?.updateValueAndValidity();
      }
    });
  }

  createEmployee() {
    if (this.employeeForm.invalid) {
      return;
    }

    const formValue = this.employeeForm.getRawValue();
    const request = {
      documentNumber: formValue.documentNumber!,
      subsidiaryPublicId: formValue.subsidiaryPublicId!,
      positionPublicId: formValue.positionPublicId!,
    };

    this.isCreatingEmployee.set(true);
    this.employeeStore.create(request);
  }
}
