import { Component, inject, OnInit, signal, effect, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { CreateContractRequest, ContractVariableValuePayload, UpdateContractRequest } from '@shared/types/contract';
import { CommonModule } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ActivatedRoute } from '@angular/router';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { CreateContractStore } from '@core/store/create-contract.store';
import { ContractStore } from '@core/store/contracts.store';
import { ContractVariableWithValidation } from '@shared/types/variable';
import { debounceTime } from 'rxjs/operators';
import { ContractListDTO } from '@shared/types/contract';
import { RetirementConceptStore } from '@core/store/retirement-concept.store';
import { HealthInsuranceConceptStore } from '@core/store/health-insurance-concept.store';

@Component({
  selector: 'app-create-contract',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    InputTextModule,
    SelectModule,
    DatePickerModule,
    ButtonModule,
    ToastModule,
    InputGroupModule,
    InputGroupAddonModule,
  ],
  templateUrl: './create-contract.html'
})
export class CreateContract implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private store = inject(CreateContractStore);
  private contractStore = inject(ContractStore);
  private retirementConceptStore = inject(RetirementConceptStore);
  private healthInsuranceConceptStore = inject(HealthInsuranceConceptStore);

  contractId = signal<string | null>(null);
  isEditMode = signal(false);
  loadingContract = signal(false);
  contractVariablesToLoad: any[] = [];
  contractDataToLoad: any = null;

  readonly loading = this.store.loading;
  readonly submitting = this.store.submitting;
  readonly contractTypes = this.store.contractTypes;
  readonly templates = this.store.templates;
  readonly subsidiaries = this.store.subsidiaries;
  readonly areas = this.store.areas;
  readonly positions = this.store.positions;
  readonly templateVariables = this.store.templateVariables;
  readonly selectedContractType = this.store.selectedContractType;
  readonly showEndDate = this.store.showEndDate;

  readonly isSearchingPerson = this.store.isSearchingPerson;
  readonly personFound = this.store.personFound;
  readonly personNotFound = this.store.personNotFound;
  readonly foundPersonData = this.store.foundPersonData;
  readonly createdContractForSigning = computed(() => {
    const contract = (this.store as any).createdContractSignal();
    return contract !== null;
  });

  contractForm = this.fb.group({
    personDocumentNumber: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    names: [{ value: '', disabled: true }, Validators.required],
    paternalSurname: [{ value: '', disabled: true }, Validators.required],
    maternalSurname: [{ value: '', disabled: true }, Validators.required],
    dateOfBirth: this.fb.control<Date | null>({ value: null, disabled: true }, Validators.required),
    startDate: this.fb.control<Date | null>(null, Validators.required),
    endDate: this.fb.control<Date | null>(null),
    subsidiaryPublicId: ['', Validators.required],
    areaPublicId: ['', Validators.required],
    positionPublicId: ['', Validators.required],
    contractTypePublicId: ['', Validators.required],
    templatePublicId: ['', Validators.required],
    photo: [null as File | null, Validators.required], // Campo obligatorio para la foto
    variables: this.fb.group({}),
    retirementConceptPublicId: [null as string | null],
    healthInsuranceConceptPublicId: [null as string | null]
  });

  photoPreview = signal<string | null>(null);
  
  readonly retirementConcepts = this.retirementConceptStore.concepts;
  readonly healthInsuranceConcepts = this.healthInsuranceConceptStore.concepts;
  readonly loadingConcepts = computed(() => 
    this.retirementConceptStore.loading() || 
    this.healthInsuranceConceptStore.loading()
  );

  validatingSubsidiarySigner = signal(false);
  subsidiarySignerError = signal<string | null>(null);

  constructor() {
    effect(() => {
      const person = this.foundPersonData();
      if (person) {
        this.contractForm.patchValue({
          names: person.names,
          paternalSurname: person.paternalLastname,
          maternalSurname: person.maternalLastname,
          dateOfBirth: person.dob ? new Date(person.dob) : null
        });
        this.contractForm.get('names')?.disable();
        this.contractForm.get('paternalSurname')?.disable();
        this.contractForm.get('maternalSurname')?.disable();
        this.contractForm.get('dateOfBirth')?.disable();

        if (!this.isEditMode()) {
          this.store.showToast('success', 'Persona encontrada', `Se encontró: ${person.names} ${person.paternalLastname}`);
        }
      }
    });

    effect(() => {
      const notFound = this.personNotFound();
      if (notFound) {
        this.contractForm.patchValue({
          names: '',
          paternalSurname: '',
          maternalSurname: '',
          dateOfBirth: null
        });
        this.contractForm.get('names')?.enable();
        this.contractForm.get('paternalSurname')?.enable();
        this.contractForm.get('maternalSurname')?.enable();
        this.contractForm.get('dateOfBirth')?.enable();

        this.store.showToast('warn', 'Persona no encontrada', 'No se encontró una persona con este documento. Complete los datos manualmente.');
      }
    });

    effect(() => {
      const variables = this.templateVariables();
      const variablesGroup = this.contractForm.get('variables') as FormGroup;

      Object.keys(variablesGroup.controls).forEach(key => variablesGroup.removeControl(key));

      variables.forEach(v => {
        const validators = [];
        if (v.isRequired) {
          validators.push(Validators.required);
        }
        if (v.validation.hasValidation && v.validation.finalRegex) {
          validators.push(Validators.pattern(v.validation.finalRegex));
        }

        let initialValue = v.defaultValue || '';
        if (this.isEditMode() && this.contractVariablesToLoad.length > 0) {
          const contractVariable = this.contractVariablesToLoad.find(cv => cv.code === v.code);
          if (contractVariable) {
            initialValue = contractVariable.value;
          }
        }

        variablesGroup.addControl(v.code, this.fb.control(initialValue, validators));
      });
    });

    effect(() => {
      const shouldShowEndDate = this.showEndDate();
      const endDateControl = this.contractForm.get('endDate');

      if (shouldShowEndDate) {
        endDateControl?.setValidators([Validators.required]);
      } else {
        endDateControl?.clearValidators();
        endDateControl?.setValue(null);
      }
      endDateControl?.updateValueAndValidity();
    });

    effect(() => {
      const createdContract = (this.store as any).createdContractSignal();
      if (createdContract && createdContract.publicId) {
        this.clearForm();
        this.store.clearCreatedContract();
        this.contractStore.navigateToContracts();
      }
    });

    effect(() => {
      const positions = this.positions();

      if (this.isEditMode() && this.contractDataToLoad && positions.length > 0) {
        const targetPositionId = this.contractDataToLoad.position?.publicId;
        if (targetPositionId) {
          const positionExists = positions.find(p => p.publicId === targetPositionId);
          if (positionExists && !this.contractForm.get('positionPublicId')?.value) {
            this.contractForm.get('positionPublicId')?.setValue(targetPositionId);
          }
        }
      }
    });
  }

  ngOnInit(): void {
    this.store.clearCreatedContract();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.contractId.set(id);
      this.isEditMode.set(true);
      this.loadContractForEdit(id);
    }

    this.store.init();
    this.setupFormListeners();
    this.loadConcepts();
  }

  loadConcepts() {
    this.retirementConceptStore.load();
    this.healthInsuranceConceptStore.load();
  }

  setupFormListeners() {
    this.contractForm.get('personDocumentNumber')?.valueChanges.pipe(
      debounceTime(500)
    ).subscribe(doc => {
      if (doc && doc.length !== 8) {
        this.clearPersonData();
      } else if (doc && doc.length === 8) {
        this.store.searchPerson(doc as string);
      } else {
        this.clearPersonData();
      }
    });

    this.contractForm.get('contractTypePublicId')?.valueChanges.subscribe(typeId => {
      this.contractForm.get('templatePublicId')?.reset('');
      if (typeId) {
        const selectedType = this.contractTypes().find(t => t.publicId === typeId);
        if (selectedType) {
          this.store.selectContractType(selectedType);
        }
      }
    });

    this.contractForm.get('areaPublicId')?.valueChanges.subscribe(areaId => {
      this.contractForm.get('positionPublicId')?.reset('');
      if (areaId) {
        const selectedArea = this.areas().find(a => a.publicId === areaId);
        if (selectedArea) {
          this.store.selectArea(selectedArea);
        }
      }
    });

    this.contractForm.get('templatePublicId')?.valueChanges.subscribe(templateId => {
      if (templateId) {
        this.store.selectTemplate(templateId as string);
      }
    });

    this.contractForm.get('subsidiaryPublicId')?.valueChanges.subscribe(subsidiaryPublicId => {
      this.subsidiarySignerError.set(null);
      if (subsidiaryPublicId) {
        this.validatingSubsidiarySigner.set(true);
        this.store.validateSubsidiarySigner(subsidiaryPublicId as string).subscribe(response => {
          this.validatingSubsidiarySigner.set(false);
          if (response?.success) {
            if (!response.data?.signatureImageUrl) {
              const errorMessage = 'El responsable de firma asignado no tiene una imagen de firma. Por favor, asigne una firma antes de crear el contrato.';
              this.subsidiarySignerError.set(errorMessage);
              this.contractForm.get('subsidiaryPublicId')?.setErrors({ noSignature: true });
            } else {
              const currentErrors = this.contractForm.get('subsidiaryPublicId')?.errors;
              if (currentErrors) {
                delete currentErrors['noSigner'];
                delete currentErrors['noSignature'];
                const hasOtherErrors = Object.keys(currentErrors).length > 0;
                if (hasOtherErrors) {
                  this.contractForm.get('subsidiaryPublicId')?.setErrors(currentErrors);
                } else {
                  this.contractForm.get('subsidiaryPublicId')?.setErrors(null);
                }
              }
            }
          } else {
            const errorMessage = 'No se encontró un responsable de firma asignado para este fundo.';
            this.subsidiarySignerError.set(errorMessage);
            this.contractForm.get('subsidiaryPublicId')?.setErrors({ noSigner: true });
          }
        });
      } else {
        this.validatingSubsidiarySigner.set(false);
      }
    });
  }

  getValidationDescription(variable: ContractVariableWithValidation): string {
    if (!variable.validation.hasValidation || variable.validation.appliedMethods.length === 0) {
      return '';
    }

    const descriptions = variable.validation.appliedMethods.map(method => {
      let desc = method.methodDescription;
      if (method.value) {
        desc += ` (${method.value})`;
      }
      return desc;
    });

    return descriptions.join(', ');
  }

  private convertToDateString(value: any): string {
    if (!value) return '';
    if (value instanceof Date) {
      return value.toISOString().split('T')[0];
    }
    if (typeof value === 'string') {
      return value;
    }
    return String(value);
  }

  private loadContractForEdit(contractId: string) {
    this.loadingContract.set(true);
    this.contractStore.getContractForCommand(contractId).subscribe((response) => {
      this.loadingContract.set(false);
      if (response.success && response.data) {
        this.populateFormWithContract(response.data);
      }
    });
  }

  private populateFormWithContract(contract: any) {
    this.contractVariablesToLoad = contract.variables || [];
    this.contractDataToLoad = contract;

    this.contractForm.patchValue({
      personDocumentNumber: contract.personDocumentNumber,
      startDate: contract.startDate ? new Date(contract.startDate) : null,
      endDate: contract.endDate ? new Date(contract.endDate) : null,
      subsidiaryPublicId: contract.subsidiary?.publicId,
      contractTypePublicId: contract.contractTypePublicId
    });

    if (contract.position?.areaPublicId) {
      this.contractForm.get('areaPublicId')?.setValue(contract.position.areaPublicId);
    }

    if (contract.template?.publicId) {
      this.contractForm.get('templatePublicId')?.setValue(contract.template.publicId);
    }

    this.contractForm.get('personDocumentNumber')?.disable();
    this.contractForm.get('names')?.disable();
    this.contractForm.get('paternalSurname')?.disable();
    this.contractForm.get('maternalSurname')?.disable();
    this.contractForm.get('dateOfBirth')?.disable();
  }

  cancel() {
    this.clearForm();
    this.store.clearCreatedContract();
    this.contractStore.navigateToContracts();
  }

  private clearForm() {
    this.contractForm.reset();
    this.photoPreview.set(null);
    const photoInput = document.getElementById('photo') as HTMLInputElement;
    if (photoInput) {
      photoInput.value = '';
    }
    this.contractForm.get('names')?.enable();
    this.contractForm.get('paternalSurname')?.enable();
    this.contractForm.get('maternalSurname')?.enable();
    this.contractForm.get('dateOfBirth')?.enable();
    this.clearPersonData();
    this.subsidiarySignerError.set(null);
    this.store.clearCreatedContract();
    this.store.clearPersonSearch();
  }

  private clearPersonData() {
    this.contractForm.patchValue({
      names: '',
      paternalSurname: '',
      maternalSurname: '',
      dateOfBirth: null
    });
    this.contractForm.get('names')?.enable();
    this.contractForm.get('paternalSurname')?.enable();
    this.contractForm.get('maternalSurname')?.enable();
    this.contractForm.get('dateOfBirth')?.enable();
  }

  submitForm() {
    if (this.contractForm.invalid) {
      this.contractForm.markAllAsTouched();
      this.store.showToast('warn', 'Formulario incompleto', 'Por favor complete todos los campos requeridos');
      return;
    }

    if (this.isEditMode()) {
      this.updateContract();
    } else {
      this.createNewContract();
    }
  }

  private createNewContract() {
    const formValue = this.contractForm.getRawValue();

    if (!formValue.photo) {
      this.store.showToast('warn', 'Foto requerida', 'Debe subir una foto para crear el contrato');
      return;
    }

    const variablesPayload: ContractVariableValuePayload[] = Object.entries(formValue.variables).map(([code, value]) => ({
      code,
      value: String(value)
    }));

    const request: CreateContractRequest = {
      personDocumentNumber: formValue.personDocumentNumber!,
      names: formValue.names!,
      paternalSurname: formValue.paternalSurname!,
      maternalSurname: formValue.maternalSurname!,
      dateOfBirth: this.convertToDateString(formValue.dateOfBirth),
      startDate: this.convertToDateString(formValue.startDate),
      endDate: formValue.endDate ? this.convertToDateString(formValue.endDate) : null,
      subsidiaryPublicId: formValue.subsidiaryPublicId!,
      positionPublicId: formValue.positionPublicId!,
      contractTypePublicId: formValue.contractTypePublicId!,
      templatePublicId: formValue.templatePublicId!,
      variables: variablesPayload,
      retirementConceptPublicId: formValue.retirementConceptPublicId || null,
      healthInsuranceConceptPublicId: formValue.healthInsuranceConceptPublicId || null
    };

    this.store.create({ request, photo: formValue.photo });
  }

  onPhotoSelected(event: any): void {
    const file = event.target?.files?.[0] as File;
    if (file) {
      this.contractForm.patchValue({ photo: file });
      
      const reader = new FileReader();
      reader.onload = (e) => {
        this.photoPreview.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  removePhoto(): void {
    this.photoPreview.set(null);
    this.contractForm.patchValue({ photo: null });
    this.contractForm.get('photo')?.markAsTouched();
    const photoInput = document.getElementById('photo') as HTMLInputElement;
    if (photoInput) {
      photoInput.value = '';
    }
  }

  private updateContract() {
    const formValue = this.contractForm.getRawValue();

    const variablesPayload: ContractVariableValuePayload[] = Object.entries(formValue.variables).map(([code, value]) => ({
      code,
      value: String(value)
    }));

    const request: UpdateContractRequest = {
      contractTypePublicId: formValue.contractTypePublicId!,
      statePublicId: '', 
      subsidiaryPublicId: formValue.subsidiaryPublicId!,
      positionPublicId: formValue.positionPublicId!,
      templatePublicId: formValue.templatePublicId!,
      startDate: this.convertToDateString(formValue.startDate),
      endDate: formValue.endDate ? this.convertToDateString(formValue.endDate) : null,
      variables: variablesPayload
    };

    this.contractStore.update({ publicId: this.contractId()!, request });
  }


}
