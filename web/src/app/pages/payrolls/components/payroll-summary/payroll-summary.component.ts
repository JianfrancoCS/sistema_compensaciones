import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { PayrollStore } from '@core/store/payroll.store';
import { LaborStore } from '@core/store/labor.store';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';
import { FormsModule } from '@angular/forms';
import { TooltipModule } from 'primeng/tooltip';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { LaborSelectOptionDTO } from '@shared/types/labor';

@Component({
  selector: 'app-payroll-summary',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    ProgressSpinnerModule,
    TableModule,
    ToastModule,
    FormsModule,
    TooltipModule,
    SelectModule,
    InputTextModule
  ],
  templateUrl: './payroll-summary.component.html',
  styleUrl: './payroll-summary.component.css'
})
export class PayrollSummaryComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  readonly payrollStore = inject(PayrollStore);
  readonly laborStore = inject(LaborStore);

  selectedLabor: string | null = null;
  employeeDocumentNumber: string = '';
  payrollPublicId: string | null = null;
  
  get laborOptions() {
    return this.laborStore.selectOptions;
  }

  ngOnInit(): void {
    const publicId = this.activatedRoute.snapshot.paramMap.get('publicId');
    if (publicId) {
      this.payrollPublicId = publicId;
      this.payrollStore.getSummary(publicId);
      this.laborStore.init(); // Inicializar el store de labores para cargar opciones
      this.loadEmployees();
    } else {
      this.goBack();
    }
  }

  loadEmployees(): void {
    if (!this.payrollPublicId) return;
    
    this.payrollStore.getPayrollEmployees({
      publicId: this.payrollPublicId,
      laborPublicId: this.selectedLabor,
      employeeDocumentNumber: this.employeeDocumentNumber || null
    });
  }

  onFilterChange(): void {
    this.loadEmployees();
  }

  clearFilters(): void {
    this.selectedLabor = null;
    this.employeeDocumentNumber = '';
    this.loadEmployees();
  }

  viewEmployeeDetail(employee: any): void {
    if (!this.payrollPublicId) return;
    
    this.router.navigate(['/system/payrolls', this.payrollPublicId, 'summary', employee.publicId]);
  }


  goBack(): void {
    this.router.navigate(['/system/payrolls']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN'
    }).format(amount);
  }
}

