import { Component, EventEmitter, inject, Input, Output, OnChanges, SimpleChanges, signal, effect } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AreaStore, AreaListDTO, UpdateAreaRequest } from '@core/store/area.store';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-area-update-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule
  ],
  templateUrl: './update-area-modal.html',
  styleUrl: './update-area-modal.css'
})
export class AreaUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() area!: AreaListDTO;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(AreaStore);

  private isUpdating = signal(false);

  areaForm = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]*$/)]]
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
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.area) {
      this.areaForm.patchValue({ name: this.area.name });
    }
  }

  hideModal() {
    this.onHide.emit();
    this.areaForm.reset();
    this.store.clearError();
  }

  updateArea() {
    if (this.areaForm.invalid) {
      this.areaForm.markAllAsTouched();
      return;
    }

    const request: UpdateAreaRequest = {
      name: this.areaForm.value.name!
    };

    this.isUpdating.set(true);
    this.store.update({ publicId: this.area.publicId, request });
  }
}
