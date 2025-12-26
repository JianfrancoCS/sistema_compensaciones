import {
  Component,
  EventEmitter,
  inject,
  Input,
  Output,
  OnChanges,
  SimpleChanges,
  signal,
  effect
} from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { SubsidiaryStore, SubsidiaryListDTO, UpdateSubsidiaryRequest } from '@core/store/subsidiary.store';
import { LocationStore } from '@core/store/location.store';
import { SelectOption } from '@core/models/api.model';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { SelectModule } from 'primeng/select';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { of } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-subsidiary-update-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    SelectModule,
    ProgressSpinnerModule,
    MessageModule
  ],
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.css'
})
export class SubsidiaryUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() subsidiary!: SubsidiaryListDTO;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected subsidiaryStore = inject(SubsidiaryStore);
  private locationStore = inject(LocationStore);

  private isUpdating = signal(false);

  subsidiaryForm = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]*$/)]],
    departmentPublicId: ['', Validators.required],
    provincePublicId: ['', Validators.required],
    districtPublicId: ['', Validators.required]
  });

  readonly departments = this.locationStore.departments;
  readonly provinces = this.locationStore.provinces;
  readonly districts = this.locationStore.districts;
  loading = signal(false);

  constructor() {
    effect(() => {
      if (!this.isUpdating()) {
        return;
      }

      if (!this.subsidiaryStore.loading()) {
        if (!this.subsidiaryStore.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.subsidiary) {
      this.loadDataForEdit();
    }
  }

  loadDataForEdit(): void {
    this.loading.set(true);
    this.subsidiaryForm.reset();
    this.subsidiaryForm.patchValue({ name: this.subsidiary.name });

    const subsidiaryDetails$ = this.subsidiaryStore.getDetails(this.subsidiary.publicId);

    subsidiaryDetails$.pipe(
      switchMap((subRes) => {
        if (!subRes.success) throw new Error('No se pudieron cargar los detalles de la sucursal.');
        return this.locationStore.getDistrictDetails(subRes.data.districtId);
      }),
      tap(districtRes => {
        if (!districtRes.success) throw new Error('No se pudieron cargar los detalles del distrito.');
        const { departmentPublicId, provincePublicId, publicId } = districtRes.data;

        this.locationStore.loadDepartments();
        this.locationStore.loadProvinces(departmentPublicId);
        this.locationStore.loadDistricts(provincePublicId);

        this.subsidiaryForm.patchValue({
          departmentPublicId,
          provincePublicId,
          districtPublicId: publicId
        }, { emitEvent: false });

        this.loading.set(false);
      })
    ).subscribe({
      error: (err) => {
        this.loading.set(false);
        this.hideModal();
      }
    });
  }

  onDepartmentChange(): void {
    const departmentId = this.subsidiaryForm.get('departmentPublicId')?.value;
    this.locationStore.resetProvinces();
    this.subsidiaryForm.patchValue({ provincePublicId: '', districtPublicId: '' });
    if (departmentId) {
      this.locationStore.loadProvinces(departmentId);
    }
  }

  onProvinceChange(): void {
    const provinceId = this.subsidiaryForm.get('provincePublicId')?.value;
    this.locationStore.resetDistricts();
    this.subsidiaryForm.patchValue({ districtPublicId: '' });
    if (provinceId) {
      this.locationStore.loadDistricts(provinceId);
    }
  }

  hideModal() {
    this.onHide.emit();
    this.subsidiaryForm.reset(); // Añadido para limpiar el formulario al cerrar
    this.subsidiaryStore.clearError();
    this.locationStore.clearError();
  }

  updateSubsidiary() {
    if (this.subsidiaryForm.invalid) {
      this.subsidiaryForm.markAllAsTouched();
      return;
    }

    const request: UpdateSubsidiaryRequest = {
      name: this.subsidiaryForm.value.name!,
      districtPublicId: this.subsidiaryForm.value.districtPublicId!
    };

    this.isUpdating.set(true);
    this.subsidiaryStore.update({ publicId: this.subsidiary.publicId, request });
  }
}
