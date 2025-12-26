import { Component, inject, signal } from '@angular/core';
import { PeriodStore, PeriodDTO } from '@core/store/period.store';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { CreatePeriodModal } from './components/create-period-modal/create-period-modal';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { DateDisplayComponent } from '@shared/dummys';

@Component({
  selector: 'app-periods',
  templateUrl: './periods.html',
  styleUrls: ['./periods.css'],
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    ToastModule,
    ConfirmDialogModule,
    TableModule,
    TooltipModule,
    CreatePeriodModal,
    ProgressSpinnerModule,
    DateDisplayComponent
  ]
})
export class Periods {
  protected store = inject(PeriodStore);
  private confirmationService = inject(ConfirmationService);

  readonly periods = this.store.periods;
  readonly loading = this.store.loading;
  readonly isEmpty = this.store.isEmpty;

  readonly isCreateModalVisible = signal(false);

  constructor() {
    this.store.init();
  }

  showCreateModal(): void {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal(): void {
    this.isCreateModalVisible.set(false);
  }

  confirmDelete(period: PeriodDTO): void {
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                       'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    const periodName = `${monthNames[period.month - 1]} ${period.year}`;

    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el período ${periodName}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(period.publicId);
      }
    });
  }

  getMonthName(month: number): string {
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                       'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    return monthNames[month - 1];
  }
}