import { Component, EventEmitter, Input, Output, inject, OnChanges, SimpleChanges, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { JustificationStore, JustificationListDTO } from '@core/store/justification.store';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { TextareaModule } from 'primeng/textarea';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { MessageModule } from 'primeng/message';
import { UpdateJustificationRequest } from '@shared/types/justification';

@Component({
  selector: 'app-update-justification-modal',
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
  templateUrl: './update-modal.html',
  styleUrl: './update-modal.css'
})
export class UpdateJustificationModalComponent implements OnChanges {
  @Input() visible: boolean = false;
  @Input() justification!: JustificationListDTO;
  @Output() onHide = new EventEmitter<void>();

  private readonly fb = inject(FormBuilder);
  protected readonly store = inject(JustificationStore);

  private isUpdating = signal(false);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(500)]],
    isPaid: [false, Validators.required]
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
    if (changes['visible'] && this.visible && this.justification) {
      this.form.patchValue(this.justification);
    }
  }

  hideModal(): void {
    this.onHide.emit();
    this.form.reset();
    this.store.clearError();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.justification) {
      const request: UpdateJustificationRequest = {
        name: this.form.value.name!,
        description: this.form.value.description!,
        isPaid: this.form.value.isPaid!
      };
      this.isUpdating.set(true);
      this.store.update({ publicId: this.justification.publicId, request });
    }
  }
}
