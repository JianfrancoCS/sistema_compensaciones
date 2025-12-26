import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreatePeriodRequest } from '@shared/types/period';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { PeriodStore } from '@core/store/period.store';
import { MessageModule } from 'primeng/message';
import { DatePickerModule } from 'primeng/datepicker';

@Component({
  selector: 'app-create-period-modal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule,
    DatePickerModule
  ],
  templateUrl: './create-period-modal.html',
  styleUrl: './create-period-modal.css'
})
export class CreatePeriodModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(PeriodStore);

  private isCreating = signal(false);

  periodForm = this.fb.group({
    explicitStartDate: [null as Date | null]
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
    this.periodForm.reset();
    this.store.clearError();
  }

  createPeriod() {
    const request: CreatePeriodRequest = {};

    const startDate = this.periodForm.value.explicitStartDate;
    if (startDate) {
      request.explicitStartDate = this.formatDate(startDate);
    }

    this.isCreating.set(true);
    this.store.create(request);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}