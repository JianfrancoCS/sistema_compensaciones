import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PayrollConfigurationStore } from '@core/store/payroll-configuration.store';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule } from '@angular/forms';
import { MultiSelectModule } from 'primeng/multiselect';
import { Router, RouterLink } from '@angular/router';
import { DateDisplayComponent } from '@shared/dummys/date-display.component';

@Component({
  selector: 'app-payroll-configuration-detail',
  standalone: true,
  imports: [CommonModule, ButtonModule, DialogModule, InputTextModule, FormsModule, MultiSelectModule, DateDisplayComponent, RouterLink],
  templateUrl: './payroll-configuration-detail.component.html',
})
export class PayrollConfigurationDetailComponent implements OnInit {
  readonly store = inject(PayrollConfigurationStore);
  private router = inject(Router);

  displayDeleteConfirm = signal<boolean>(false);

  assignedConcepts = computed(() =>
    this.store.conceptAssignments().filter(c => c.isAssigned)
  );

  ngOnInit(): void {
    this.store.init();
  }

  showDeleteConfirm(): void {
    this.displayDeleteConfirm.set(true);
  }

  hideDeleteConfirm(): void {
    this.displayDeleteConfirm.set(false);
  }

  deleteConfiguration(): void {
    this.store.deleteActivePayrollConfiguration();
    this.hideDeleteConfirm();
  }

  navigateToCreateConfiguration(): void {
    this.router.navigate(['/system/payroll-configurations/create']);
  }

  navigateToEditConcepts(): void {
    this.router.navigate(['/system/payroll-configurations/edit']);
  }
}
