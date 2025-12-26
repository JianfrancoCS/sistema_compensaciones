import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateAreaRequest } from '@shared/types/area';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { AreaStore } from '../../../../core/store/area.store';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-area-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule
  ],
  templateUrl: './create-area-modal.html',
  styleUrl: './create-area-modal.css'
})
export class AreaCreateModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(AreaStore);

  private isCreating = signal(false);

  areaForm = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z\s]*$/)]]
  });

  constructor() {
    effect(() => {
      if (!this.isCreating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isCreating.set(false);
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.areaForm.reset();
    this.store.clearError();
  }

  createArea() {
    if (this.areaForm.valid) {
      const request: CreateAreaRequest = {
        name: this.areaForm.value.name!
      };
      this.isCreating.set(true);
      this.store.create(request);
    }
  }
}
