import {
  Component,
  EventEmitter,
  inject,
  Input,
  Output,
  effect,
  OnInit,
  OnDestroy,
  signal
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { SubsidiaryStore } from '@core/store/subsidiary.store';
import { LocationStore } from '@core/store/location.store';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { Subject } from 'rxjs';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-subsidiary-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    SelectModule,
    MessageModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class SubsidiaryCreateModal implements OnInit, OnDestroy {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();
  @Output() onSuccess = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected subsidiaryStore = inject(SubsidiaryStore);
  private locationStore = inject(LocationStore);
  private destroy$ = new Subject<void>();

  private isCreating = signal(false);

  readonly departments = this.locationStore.departments;
  readonly provinces = this.locationStore.provinces;
  readonly districts = this.locationStore.districts;

  readonly subsidiaryForm = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]*$/)]],
    departmentPublicId: ['', Validators.required],
    provincePublicId: ['', Validators.required],
    districtPublicId: ['', Validators.required]
  });

  constructor() {
    effect(() => {
      if (!this.isCreating()) {
        return;
      }

      if (!this.subsidiaryStore.loading()) {
        if (!this.subsidiaryStore.error()) {
          this.hideModal();
        }
        this.isCreating.set(false);
      }
    });
  }

  private departmentsEffect = effect(() => {
    if (this.visible) {
      this.locationStore.loadDepartments();
    }
  });

  ngOnInit(): void {
    this.locationStore.loadDepartments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onDepartmentChange(event: any): void {
    const departmentId = event.value;
    this.subsidiaryForm.patchValue({ provincePublicId: '', districtPublicId: '' });

    this.locationStore.resetProvinces();

    if (departmentId) {
      this.locationStore.loadProvinces(departmentId);
    }
  }

  onProvinceChange(event: any): void {
    const provinceId = event.value;
    this.subsidiaryForm.patchValue({ districtPublicId: '' });

    this.locationStore.resetDistricts();

    if (provinceId) {
      this.locationStore.loadDistricts(provinceId);
    }
  }

  hideModal(): void {
    this.onHide.emit();
    this.subsidiaryForm.reset();
    this.locationStore.resetProvinces();
    this.subsidiaryStore.clearError();
  }

  createSubsidiary(): void {
    if (this.subsidiaryForm.invalid) {
      this.subsidiaryForm.markAllAsTouched();
      return;
    }

    const { name, districtPublicId } = this.subsidiaryForm.value;

    this.isCreating.set(true);
    this.subsidiaryStore.create({
      name: name!,
      districtPublicId: districtPublicId!
    });
  }
}
