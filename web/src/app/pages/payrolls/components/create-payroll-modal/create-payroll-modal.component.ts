import { Component, input, output, inject, OnInit, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PayrollStore } from '@core/store/payroll.store';
import { SubsidiaryStore } from '@core/store/subsidiary.store';
import { PeriodStore } from '@core/store/period.store';
import { SelectModule } from 'primeng/select';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';

@Component({
  selector: 'app-create-payroll-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SelectModule,
    ModalTemplateComponent
  ],
  templateUrl: './create-payroll-modal.component.html',
})
export class CreatePayrollModalComponent implements OnInit {
  visible = input.required<boolean>();
  onHide = output<void>();

  private fb = inject(FormBuilder);
  readonly payrollStore = inject(PayrollStore);
  readonly subsidiaryStore = inject(SubsidiaryStore);
  readonly periodStore = inject(PeriodStore);

  payrollForm: FormGroup;

  subsidiaryOptions = computed(() => {
    return this.subsidiaryStore.subsidiaries().map(s => ({
      label: s.name,
      value: s.publicId
    }));
  });

  periodOptions = computed(() => {
    return this.periodStore.selectOptions().map(p => ({
      label: p.name,
      value: p.publicId
    }));
  });

  constructor() {
    this.payrollForm = this.fb.group({
      subsidiaryPublicId: ['', Validators.required],
      periodPublicId: ['', Validators.required],
    });

    effect(() => {
      if (this.visible()) {
        this.subsidiaryStore.init();
        this.periodStore.loadSelectOptions();
      }
    });
  }

  ngOnInit(): void {
    this.subsidiaryStore.init();
    this.periodStore.loadSelectOptions();
  }

  createPayroll(): void {
    if (this.payrollForm.valid) {
      const formValue = this.payrollForm.value;
      const payload = {
        subsidiaryPublicId: formValue.subsidiaryPublicId,
        payrollPeriodPublicId: formValue.periodPublicId
      };
      this.payrollStore.createPayroll(payload);
      this.hideModal();
    }
  }

  hideModal(): void {
    this.payrollForm.reset();
    this.onHide.emit();
  }
}
