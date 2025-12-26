import { Component, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CompanyStore } from '@core/store/company.store';
import { DateDisplayComponent } from '@shared/dummys';

@Component({
  selector: 'app-company-info-display',
  standalone: true,
  imports: [CommonModule, ButtonModule, DateDisplayComponent],
  templateUrl: './company-info-display.html'
})
export class CompanyInfoDisplay {
  readonly companyStore = inject(CompanyStore);
  readonly company = this.companyStore.company;

  @Output() onEdit = new EventEmitter<void>();

  editCompany(): void {
    this.onEdit.emit();
  }
}