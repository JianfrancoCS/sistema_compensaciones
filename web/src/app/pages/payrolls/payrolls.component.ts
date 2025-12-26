import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PayrollStore } from '@core/store/payroll.store';
import { SubsidiaryStore } from '@core/store/subsidiary.store';
import { PeriodStore } from '@core/store/period.store';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService } from 'primeng/api';
import { PayrollListDTO, PayrollStatus } from '@shared/types/payroll';
import { PaginatorModule } from 'primeng/paginator';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { Router } from '@angular/router';
import { CreatePayrollModalComponent } from './components/create-payroll-modal/create-payroll-modal.component';
import { DateDisplayComponent } from '@shared/dummys';

@Component({
  selector: 'app-payrolls',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    ProgressSpinnerModule,
    ConfirmDialogModule,
    ToastModule,
    PaginatorModule,
    SelectModule,
    TableModule,
    TooltipModule,
    CreatePayrollModalComponent,
    DateDisplayComponent
  ],
  templateUrl: './payrolls.component.html',
})
export class PayrollsComponent implements OnInit {
  readonly store = inject(PayrollStore);
  readonly subsidiaryStore = inject(SubsidiaryStore);
  readonly periodStore = inject(PeriodStore);
  private confirmationService = inject(ConfirmationService);
  private router = inject(Router);

  isCreateModalVisible = signal(false);

  selectedSubsidiary: string | null = null;
  selectedPeriod: string | null = null;
  selectedStatus: PayrollStatus | null = null;

  subsidiaryOptions = computed(() => {
    return this.subsidiaryStore.subsidiaries().map(s => ({
      label: s.name,
      value: s.publicId
    }));
  });

  periodOptions = computed(() => {
    return this.periodStore.periods().map(p => ({
      label: `${this.getMonthName(p.month)} ${p.year}`,
      value: p.publicId
    }));
  });

  statusOptions = [
    { label: 'Borrador', value: 'BORRADOR' as PayrollStatus },
    { label: 'Calculando', value: 'CALCULANDO' as PayrollStatus },
    { label: 'Calculada', value: 'CALCULADA' as PayrollStatus },
    { label: 'Cerrada', value: 'CERRADA' as PayrollStatus },
    { label: 'Anulada', value: 'ANULADA' as PayrollStatus }
  ];

  getStatusForFilter(stateName: string): PayrollStatus | null {
    const state = stateName?.toUpperCase();
    const statusMap: Record<string, PayrollStatus> = {
      'DRAFT': 'BORRADOR',
      'IN PROGRESS': 'CALCULANDO',
      'CALCULATED': 'CALCULADA',
      'APPROVED': 'CERRADA',
      'CANCELLED': 'ANULADA',
    };
    return statusMap[state] || null;
  }

  ngOnInit(): void {
    this.store.init();
    this.subsidiaryStore.init();
    this.periodStore.init();
  }

  showCreateModal(): void {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal(): void {
    this.isCreateModalVisible.set(false);
  }

  onSubsidiaryFilterChange(value: string | null): void {
    this.store.setSubsidiaryFilter(value);
  }

  onPeriodFilterChange(value: string | null): void {
    this.store.setPeriodFilter(value);
  }

  onStatusFilterChange(value: PayrollStatus | null): void {
    this.store.setStatusFilter(value);
  }

  clearFilters(): void {
    this.selectedSubsidiary = null;
    this.selectedPeriod = null;
    this.selectedStatus = null;
    this.store.clearFilters();
  }

  confirmLaunch(payroll: PayrollListDTO): void {
    const monthName = payroll.periodMonth ? this.getMonthName(payroll.periodMonth) : 'N/A';
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres lanzar el cálculo de la planilla de ${payroll.subsidiaryName} - ${monthName} ${payroll.periodYear || ''}?`,
      header: 'Confirmar Cálculo',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.launchPayroll(payroll.publicId);
      }
    });
  }

  confirmDelete(payroll: PayrollListDTO): void {
    const monthName = payroll.periodMonth ? this.getMonthName(payroll.periodMonth) : 'N/A';
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar la planilla de ${payroll.subsidiaryName} - ${monthName} ${payroll.periodYear || ''}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.deletePayroll(payroll.publicId);
      }
    });
  }

  onPageChange(event: any): void {
    this.store.setPage(event.page);
  }

  getStatusDisplayName(stateName: string): string {
    const state = stateName?.toUpperCase();
    const displayNames: Record<string, string> = {
      'DRAFT': 'Borrador',
      'IN PROGRESS': 'Calculando',
      'CALCULATED': 'Calculada',
      'APPROVED': 'Cerrada',
      'PAID': 'Pagada',
      'CANCELLED': 'Anulada',
      'CANCELLED FOR CORRECTION': 'Anulada para Corrección',
    };
    return displayNames[state] || stateName;
  }

  getStatusBadgeClass(stateName: string): string {
    const state = stateName?.toUpperCase();
    const classes: Record<string, string> = {
      'DRAFT': 'bg-gray-100 text-gray-800 border-gray-300',
      'IN PROGRESS': 'bg-blue-100 text-blue-800 border-blue-300',
      'CALCULATED': 'bg-green-100 text-green-800 border-green-300',
      'APPROVED': 'bg-purple-100 text-purple-800 border-purple-300',
      'PAID': 'bg-indigo-100 text-indigo-800 border-indigo-300',
      'CANCELLED': 'bg-red-100 text-red-800 border-red-300',
      'CANCELLED FOR CORRECTION': 'bg-orange-100 text-orange-800 border-orange-300',
    };
    return classes[state] || 'bg-gray-100 text-gray-800';
  }

  getStatusIcon(stateName: string): string {
    const state = stateName?.toUpperCase();
    const icons: Record<string, string> = {
      'DRAFT': 'pi-file-edit',
      'IN PROGRESS': 'pi-spin pi-spinner',
      'CALCULATED': 'pi-check-circle',
      'APPROVED': 'pi-lock',
      'PAID': 'pi-money-bill',
      'CANCELLED': 'pi-times-circle',
      'CANCELLED FOR CORRECTION': 'pi-exclamation-triangle',
    };
    return icons[state] || 'pi-file';
  }

  confirmGeneratePayslips(payroll: PayrollListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres generar las boletas de pago para la planilla ${payroll.code}?`,
      header: 'Confirmar Generación de Boletas',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.generatePayslips(payroll.publicId);
      }
    });
  }

  confirmCancel(payroll: PayrollListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres anular la planilla ${payroll.code}? Esta acción no se puede deshacer.`,
      header: 'Confirmar Anulación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.cancelPayroll(payroll.publicId);
      }
    });
  }

  getMonthName(month: number): string {
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                       'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    return monthNames[month - 1];
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN'
    }).format(amount);
  }

  viewSummary(payroll: PayrollListDTO): void {
    this.router.navigate(['/system/payrolls', payroll.publicId, 'summary']);
  }
}