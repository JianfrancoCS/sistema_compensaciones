import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartModule } from 'primeng/chart';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DashboardService } from '@core/services/dashboard.service';
import { SubsidiaryStore } from '@core/store/subsidiary.store';
import { PeriodStore } from '@core/store/period.store';
import { DashboardFilters, DashboardStatsDTO } from '@shared/types/dashboard';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ChartModule,
    SelectModule,
    DatePickerModule,
    CardModule,
    ButtonModule,
    ToastModule,
    ProgressSpinnerModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
  providers: [MessageService]
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private subsidiaryStore = inject(SubsidiaryStore);
  private periodStore = inject(PeriodStore);
  private messageService = inject(MessageService);

  loading = signal(true);
  loadingCharts = signal(true);

  selectedSubsidiary = signal<string | null>(null);
  selectedPeriod = signal<string | null>(null);
  dateFrom = signal<Date | null>(null);
  dateTo = signal<Date | null>(null);

  stats = signal<DashboardStatsDTO | null>(null);

  payrollsByStatusData: any = null;
  payrollsByStatusOptions: any = null;

  employeesBySubsidiaryData: any = null;
  employeesBySubsidiaryOptions: any = null;

  payrollsByPeriodData: any = null;
  payrollsByPeriodOptions: any = null;

  tareosByLaborData: any = null;
  tareosByLaborOptions: any = null;

  attendanceTrendData: any = null;
  attendanceTrendOptions: any = null;

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

  ngOnInit(): void {
    this.subsidiaryStore.init();
    this.periodStore.init();
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading.set(true);
    this.loadingCharts.set(true);

    const filters: DashboardFilters = {
      subsidiaryPublicId: this.selectedSubsidiary() || undefined,
      periodPublicId: this.selectedPeriod() || undefined,
      dateFrom: this.dateFrom() ? this.dateFrom()!.toISOString().split('T')[0] : undefined,
      dateTo: this.dateTo() ? this.dateTo()!.toISOString().split('T')[0] : undefined
    };

    this.dashboardService.getStats(filters).subscribe({
      next: (result) => {
        if (result.success && result.data) {
          this.stats.set(result.data);
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar estadísticas'
        });
        this.loading.set(false);
      }
    });

    this.loadCharts(filters);
  }

  loadCharts(filters: DashboardFilters): void {
    this.loadingCharts.set(true);

    this.dashboardService.getPayrollsByStatus(filters).subscribe({
      next: (result) => {
        if (result.success && result.data) {
          this.updatePayrollsByStatusChart(result.data);
        }
      }
    });

    this.dashboardService.getEmployeesBySubsidiary(filters).subscribe({
      next: (result) => {
        if (result.success && result.data) {
          this.updateEmployeesBySubsidiaryChart(result.data);
        }
      }
    });

    this.dashboardService.getPayrollsByPeriod(filters).subscribe({
      next: (result) => {
        if (result.success && result.data) {
          this.updatePayrollsByPeriodChart(result.data);
        }
      }
    });

    this.dashboardService.getTareosByLabor(filters).subscribe({
      next: (result) => {
        if (result.success && result.data) {
          this.updateTareosByLaborChart(result.data);
        }
      }
    });

    this.dashboardService.getAttendanceTrend(filters).subscribe({
      next: (result) => {
        if (result.success && result.data) {
          this.updateAttendanceTrendChart(result.data);
        }
        this.loadingCharts.set(false);
      }
    });
  }

  updatePayrollsByStatusChart(data: any[]): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.payrollsByStatusData = {
      labels: data.map(item => this.getStatusLabel(item.status)),
      datasets: [
        {
          data: data.map(item => item.count),
          backgroundColor: [
            'rgba(54, 162, 235, 0.5)',
            'rgba(255, 99, 132, 0.5)',
            'rgba(255, 205, 86, 0.5)',
            'rgba(75, 192, 192, 0.5)',
            'rgba(153, 102, 255, 0.5)'
          ],
          borderColor: [
            'rgb(54, 162, 235)',
            'rgb(255, 99, 132)',
            'rgb(255, 205, 86)',
            'rgb(75, 192, 192)',
            'rgb(153, 102, 255)'
          ]
        }
      ]
    };

    this.payrollsByStatusOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      }
    };
  }

  updateEmployeesBySubsidiaryChart(data: any[]): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.employeesBySubsidiaryData = {
      labels: data.map(item => item.subsidiaryName),
      datasets: [
        {
          label: 'Empleados',
          data: data.map(item => item.count),
          backgroundColor: 'rgba(54, 162, 235, 0.5)',
          borderColor: 'rgb(54, 162, 235)',
          borderWidth: 1
        }
      ]
    };

    this.employeesBySubsidiaryOptions = {
      indexAxis: 'y',
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        }
      }
    };
  }

  updatePayrollsByPeriodChart(data: any[]): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.payrollsByPeriodData = {
      labels: data.map(item => item.period),
      datasets: [
        {
          label: 'Cantidad',
          data: data.map(item => item.count),
          backgroundColor: 'rgba(54, 162, 235, 0.5)',
          borderColor: 'rgb(54, 162, 235)',
          borderWidth: 1
        },
        {
          label: 'Monto Total',
          data: data.map(item => item.totalAmount),
          backgroundColor: 'rgba(75, 192, 192, 0.5)',
          borderColor: 'rgb(75, 192, 192)',
          borderWidth: 1,
          yAxisID: 'y1'
        }
      ]
    };

    this.payrollsByPeriodOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        },
        y: {
          type: 'linear',
          display: true,
          position: 'left',
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        },
        y1: {
          type: 'linear',
          display: true,
          position: 'right',
          ticks: {
            color: textColorSecondary
          },
          grid: {
            drawOnChartArea: false
          }
        }
      }
    };
  }

  updateTareosByLaborChart(data: any[]): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.tareosByLaborData = {
      labels: data.map(item => item.laborName),
      datasets: [
        {
          label: 'Tareos',
          data: data.map(item => item.count),
          backgroundColor: 'rgba(255, 159, 64, 0.5)',
          borderColor: 'rgb(255, 159, 64)',
          borderWidth: 1
        },
        {
          label: 'Empleados',
          data: data.map(item => item.employeeCount),
          backgroundColor: 'rgba(153, 102, 255, 0.5)',
          borderColor: 'rgb(153, 102, 255)',
          borderWidth: 1
        }
      ]
    };

    this.tareosByLaborOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        }
      }
    };
  }

  updateAttendanceTrendChart(data: any[]): void {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    this.attendanceTrendData = {
      labels: data.map(item => new Date(item.date).toLocaleDateString('es-PE', { month: 'short', day: 'numeric' })),
      datasets: [
        {
          label: 'Entradas',
          data: data.map(item => item.entries),
          fill: false,
          borderColor: 'rgb(75, 192, 192)',
          tension: 0.4
        },
        {
          label: 'Salidas',
          data: data.map(item => item.exits),
          fill: false,
          borderColor: 'rgb(255, 99, 132)',
          tension: 0.4
        }
      ]
    };

    this.attendanceTrendOptions = {
      plugins: {
        legend: {
          labels: {
            color: textColor
          }
        }
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        },
        y: {
          ticks: {
            color: textColorSecondary
          },
          grid: {
            color: surfaceBorder
          }
        }
      }
    };
  }

  onSubsidiaryChange(): void {
    this.loadDashboard();
  }

  onPeriodChange(): void {
    this.loadDashboard();
  }

  onDateFromChange(): void {
    this.loadDashboard();
  }

  onDateToChange(): void {
    this.loadDashboard();
  }

  clearFilters(): void {
    this.selectedSubsidiary.set(null);
    this.selectedPeriod.set(null);
    this.dateFrom.set(null);
    this.dateTo.set(null);
    this.loadDashboard();
  }

  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      'BORRADOR': 'Borrador',
      'CALCULANDO': 'Calculando',
      'CALCULADA': 'Calculada',
      'CERRADA': 'Cerrada',
      'ANULADA': 'Anulada'
    };
    return labels[status] || status;
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
}

