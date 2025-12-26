import { Component, inject, signal } from '@angular/core';
import { AreaStore, AreaListDTO } from '../../core/store/area.store';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { AreaCreateModal } from './components/create-area-modal/create-area-modal';
import { AreaUpdateModal } from './components/update-modal/update-area-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-areas',
  templateUrl: './areas.html',
  styleUrls: ['./areas.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    AreaCreateModal,
    AreaUpdateModal,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule
  ]
})
export class Areas {
  protected store = inject(AreaStore);
  private confirmationService = inject(ConfirmationService);

  readonly areas = this.store.areas;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  readonly isCreateModalVisible = signal(false);
  readonly isUpdateModalVisible = signal(false);
  readonly selectedArea = signal<AreaListDTO | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.store.search(query);
  }


  loadAreas(event: TableLazyLoadEvent): void {
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

  showUpdateModal(area: AreaListDTO): void {
    this.selectedArea.set(area);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal(): void {
    this.isUpdateModalVisible.set(false);
    this.selectedArea.set(null);
  }

  confirmDelete(area: AreaListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el área ${area.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(area.publicId);
      }
    });
  }
}
