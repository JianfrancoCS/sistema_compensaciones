import { Component, inject, signal, OnInit, effect, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { CardModule } from 'primeng/card';
import { CompanyStore } from '@core/store/company.store';
import { CompanyEditModal } from './components/company-edit-modal/company-edit-modal';
import { CompanyInfoDisplay } from './components/company-info-display/company-info-display';
import { FloatLabelModule } from 'primeng/floatlabel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DividerModule } from 'primeng/divider';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-company',
  templateUrl: './company.html',
  styleUrls: ['./company.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    CardModule,
    CompanyEditModal,
    CompanyInfoDisplay,
    FloatLabelModule,
    ProgressSpinnerModule,
    DividerModule,
    ToastModule,
    MessageModule
  ]
})
export class Company implements OnInit {
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);
  readonly companyStore = inject(CompanyStore);

  readonly company = this.companyStore.company;
  readonly loading = this.companyStore.loading;
  readonly externalLookupLoading = this.companyStore.externalLookupLoading;
  readonly externalCompanyInfo = this.companyStore.externalCompanyInfo;

  isEditModalVisible = signal(false);
  createForm!: FormGroup;

  constructor() {
    this.buildForm();

    effect(() => {
      const externalInfo = this.externalCompanyInfo();
      if (externalInfo) {
        this.createForm.patchValue({
          legalName: externalInfo.businessName,
          tradeName: externalInfo.tradeName,
          companyType: externalInfo.companyType,
        });
        this.createForm.get('legalName')?.enable();
        this.createForm.get('tradeName')?.enable();
        this.createForm.get('companyType')?.enable();
      }
    });

    effect(() => {
      const comp = this.company();
      if (comp) {
        this.cdr.detectChanges();
      }
    });
  }

  ngOnInit(): void {
    this.companyStore.init();
  }

  private buildForm(): void {
    this.createForm = this.fb.group({
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

  onRucInput(): void {
    const rucControl = this.createForm.get('ruc');
    if (rucControl?.valid && rucControl.value.length === 11) {
      this.companyStore.externalLookup(rucControl.value);
    }
  }

  onSubmit(): void {
    if (this.createForm.valid) {
      const formValue = this.createForm.getRawValue();
      this.companyStore.createOrUpdateCompany(formValue);
    }
  }

  showEditModal(): void {
    this.isEditModalVisible.set(true);
  }

  hideEditModal(): void {
    this.isEditModalVisible.set(false);
  }
}
