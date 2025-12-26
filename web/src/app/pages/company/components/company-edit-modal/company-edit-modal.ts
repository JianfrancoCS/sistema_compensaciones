import { Component, EventEmitter, Input, Output, inject, OnChanges, SimpleChanges, effect, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { CompanyStore } from '@core/store/company.store';
import { CompanyDTO } from '@shared/types/company';
import { FloatLabelModule } from 'primeng/floatlabel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DividerModule } from 'primeng/divider';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-company-edit-modal',
  templateUrl: './company-edit-modal.component.html',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    FloatLabelModule,
    ProgressSpinnerModule,
    DividerModule,
    MessageModule
  ]
})
export class CompanyEditModal implements OnChanges {
  @Input() visible = false;
  @Input() company: CompanyDTO | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();

  private fb = inject(FormBuilder);
  readonly companyStore = inject(CompanyStore);

  editForm!: FormGroup;
  private originalRuc: string | null = null;
  private isSaving = signal(false);

  constructor() {
    this.buildForm();

    effect(() => {
      const externalInfo = this.companyStore.externalCompanyInfo();
      if (externalInfo && this.editForm.get('ruc')?.value === externalInfo.ruc) {
        this.editForm.patchValue({
          legalName: externalInfo.businessName,
          tradeName: externalInfo.tradeName,
          companyType: externalInfo.companyType,
        });
      }
    });

    effect(() => {
      if (!this.isSaving()) {
        return;
      }

      if (!this.companyStore.loading()) {
        if (!this.companyStore.error()) {
          this.onHide();
        }
        this.isSaving.set(false);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible && this.company) {
      this.loadCompanyData();
    }
  }

  private buildForm(): void {
    this.editForm = this.fb.group({
      legalName: [{ value: '', disabled: true }, [Validators.required, Validators.maxLength(255)]],
      tradeName: [{ value: '', disabled: true }, [Validators.required, Validators.maxLength(255)]],
      ruc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      companyType: [{ value: '', disabled: true }, [Validators.required, Validators.maxLength(100)]],
      paymentIntervalDays: [15, [Validators.required, Validators.min(1)]],
      maxMonthlyWorkingHours: [null, [Validators.min(1)]],
      payrollDeclarationDay: [5, [Validators.required, Validators.min(1), Validators.max(28)]],
      payrollAnticipationDays: [2, [Validators.required, Validators.min(0)]],
      overtimeRate: [0.25, [Validators.required, Validators.min(0), Validators.max(1)]],
      dailyNormalHours: [8, [Validators.required, Validators.min(1), Validators.max(24)]],
      monthCalculationDays: [30, [Validators.required, Validators.min(28), Validators.max(31)]],
    });
  }

  private loadCompanyData(): void {
    if (this.company) {
      this.originalRuc = this.company.ruc;
      this.editForm.patchValue(this.company);
    }
  }

  onRucInput(): void {
    const rucControl = this.editForm.get('ruc');
    if (rucControl?.valid && rucControl.value !== this.originalRuc) {
      this.companyStore.externalLookup(rucControl.value);
    }
  }

  onHide(): void {
    if (!this.companyStore.loading() && !this.companyStore.externalLookupLoading()) {
      this.visibleChange.emit(false);
      this.editForm.reset();
      this.originalRuc = null;
    }
  }

  onSubmit(): void {
    if (this.editForm.invalid || this.companyStore.loading() || this.companyStore.externalLookupLoading()) {
      return;
    }
    this.isSaving.set(true);
    const request = this.editForm.getRawValue();
    this.companyStore.createOrUpdateCompany(request);
  }
}
