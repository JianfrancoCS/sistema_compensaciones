import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { JustificationStore } from '@core/store/justification.store';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { TextareaModule } from 'primeng/textarea';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { MessageModule } from 'primeng/message';
import { CreateJustificationRequest } from '@shared/types/justification';

@Component({
  selector: 'app-create-justification-modal',
  standalone: true,
  imports: [
    CommonModule,
    InputTextModule,
    ReactiveFormsModule,
    ToggleSwitchModule,
    TextareaModule,
    ModalTemplateComponent,
    MessageModule
  ],
  templateUrl: './create-modal.html',
  styleUrl: './create-modal.css'
})
export class CreateJustificationModalComponent {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  protected readonly store = inject(JustificationStore);

  private isCreating = signal(false);

  justificationForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(500)]],
    isPaid: [false, Validators.required]
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

  hideModal(): void {
    this.onHide.emit();
    this.justificationForm.reset();
    this.store.clearError();
  }

  onSubmit(): void {
    if (this.justificationForm.valid) {
      const request: CreateJustificationRequest = {
        name: this.justificationForm.value.name!,
        description: this.justificationForm.value.description!,
        isPaid: this.justificationForm.value.isPaid!
      };
      this.isCreating.set(true);
      this.store.create(request);
    } else {
      this.justificationForm.markAllAsTouched();
    }
  }
}
