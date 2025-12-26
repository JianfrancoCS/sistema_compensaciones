import { Component, inject, signal } from '@angular/core';
import { LaborUnitStore, LaborUnitListDTO } from '@core/store/labor-unit.store';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { DateDisplayComponent } from '@shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { LaborUnitCreateModal } from './components/create-modal/create-modal';
import { LaborUnitUpdateModal } from './components/update-modal/update-modal';

@Component({
  selector: 'app-labor-units',
  templateUrl: './labor-units.html',
  styleUrls: ['./labor-units.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule,
    LaborUnitCreateModal,
    LaborUnitUpdateModal
  ]
})
export class LaborUnits {
  protected store = inject(LaborUnitStore);
  private confirmationService = inject(ConfirmationService);

  readonly laborUnits = this.store.laborUnits;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  readonly isCreateModalVisible = signal(false);
  readonly isUpdateModalVisible = signal(false);
  readonly selectedLaborUnit = signal<LaborUnitListDTO | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.store.search(query);
  }


  loadLaborUnits(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const pageSize = event.rows ?? 10;
    const sortBy = Array.isArray(event.sortField)
      ? event.sortField[0]
      : event.sortField || 'createdAt';
    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    this.store.loadPage({ page, pageSize, sortBy, sortDirection });
  }

  showCreateModal(): void {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal(): void {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(laborUnit: LaborUnitListDTO): void {
    this.selectedLaborUnit.set(laborUnit);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal(): void {
    this.isUpdateModalVisible.set(false);
    this.selectedLaborUnit.set(null);
  }

  confirmDelete(laborUnit: LaborUnitListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar la unidad de labor ${laborUnit.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(laborUnit.publicId);
      }
    });
  }
}
