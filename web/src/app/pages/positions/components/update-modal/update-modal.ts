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
import { InputTextModule }from 'primeng/inputtext';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {
  Position as PositionListDTO,
  UpdatePositionRequest
} from '@shared/types/position';
import { PositionStore } from '@core/store/position.store';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { CheckboxModule } from 'primeng/checkbox';
import { SelectModule } from 'primeng/select';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-position-update-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    CommonModule,
    FormsModule,
    ModalTemplateComponent,
    CheckboxModule,
    SelectModule,
    MessageModule
  ],
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.css'
})
export class PositionUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() position!: PositionListDTO;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(PositionStore);

  private isUpdating = signal(false);

  readonly areaOptions = this.store.areaSelectOptions;
  readonly managerPositionOptions = this.store.positionSelectOptions;

  filterByArea: boolean = false;

  positionForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z\s]*$/)]],
    areaPublicId: ['', Validators.required],
    salary: ['', [Validators.required, Validators.min(0.01)]],
    requiresManager: [false],
    requiredManagerPositionPublicId: [null as string | null | undefined],
    unique: [false]
  });

  constructor() {
    effect(() => {
      if (!this.isUpdating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
      }
    });

    effect(() => {
      const requiresManager = this.positionForm.get('requiresManager')?.value;
      const requiredManagerPositionPublicIdControl = this.positionForm.get('requiredManagerPositionPublicId');

      if (requiresManager) {
        requiredManagerPositionPublicIdControl?.addValidators(Validators.required);
      } else {
        requiredManagerPositionPublicIdControl?.removeValidators(Validators.required);
        requiredManagerPositionPublicIdControl?.patchValue(null);
      }
      requiredManagerPositionPublicIdControl?.updateValueAndValidity();
    });

    effect(() => {
      const areaPublicId = this.positionForm.get('areaPublicId')?.value;
      if (this.filterByArea && areaPublicId) {
        this.store.loadPositionSelectOptionsByArea(areaPublicId);
      } else if (!this.filterByArea) {
        this.store.loadPositionSelectOptionsByArea(undefined);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.position) {
      this.loadPositionData(this.position.publicId);
    }
  }

  loadPositionData(publicId: string): void {
    this.store.getDetails(publicId).subscribe((response) => {
      if (response.success) {
        const positionDetails = response.data;
        this.positionForm.patchValue({
          name: positionDetails.name,
          areaPublicId: positionDetails.areaPublicId,
          salary: positionDetails.salary ? positionDetails.salary.toString() : '',
          requiresManager: positionDetails.requiresManager,
          requiredManagerPositionPublicId: positionDetails.requiredManagerPositionPublicId || null,
          unique: positionDetails.unique
        });

        if (positionDetails.requiredManagerPositionPublicId) {
          this.filterByArea = true;
        } else {
          this.filterByArea = false;
        }

        this.store.loadPositionSelectOptionsByArea(positionDetails.areaPublicId);
      }
    });
  }

  onFilterByAreaChange(): void {
    const areaPublicId = this.positionForm.get('areaPublicId')?.value;
    if (this.filterByArea && areaPublicId) {
      this.store.loadPositionSelectOptionsByArea(areaPublicId);
    } else {
      this.store.loadPositionSelectOptionsByArea(undefined);
    }
  }

  hideModal() {
    this.onHide.emit();
    this.positionForm.reset();
    this.filterByArea = false;
    this.store.clearError();
  }

  updatePosition() {
    if (this.positionForm.invalid) {
      this.positionForm.markAllAsTouched();
      return;
    }

    const { name, areaPublicId, salary, requiresManager, requiredManagerPositionPublicId, unique } = this.positionForm.value;

    const request: UpdatePositionRequest = {
      name: name!,
      areaPublicId: areaPublicId!,
      salary: parseFloat(salary as string),
      requiresManager: requiresManager!,
      requiredManagerPositionPublicId: requiredManagerPositionPublicId || null,
      unique: unique!
    };

    this.isUpdating.set(true);
    this.store.update({ publicId: this.position.publicId, request });
  }
}
