import { Component, computed, inject, signal, effect } from '@angular/core';
import { PositionStore } from '../../core/store/position.store';
import { Position as PositionListDTO } from '@shared/types/position';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CommonModule } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { PositionCreateModal } from './components/create-modal/create-modal';
import { PositionUpdateModal } from './components/update-modal/update-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-positions',
  templateUrl: './positions.html',
  styleUrls: ['./positions.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    CommonModule,
    ToastModule,
    ConfirmDialogModule,
    PositionCreateModal,
    PositionUpdateModal,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule
  ],
  providers: [ConfirmationService]
})
export class Positions {
  protected store = inject(PositionStore);
  private confirmationService = inject(ConfirmationService);

  readonly positions = this.store.positions;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  isCreateModalVisible = signal(false);
  isUpdateModalVisible = signal(false);
  selectedPosition = signal<PositionListDTO | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.store.search(inputElement.value);
  }

  loadPositions(event: TableLazyLoadEvent) {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const pageSize = event.rows ?? 10;

    let sortBy = 'createdAt';
    if (event.sortField) {
      if (Array.isArray(event.sortField)) {
        sortBy = event.sortField[0] || 'createdAt';
      } else {
        sortBy = event.sortField;
      }
    }

    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    this.store.loadPage({ page, pageSize, sortBy, sortDirection });
  }

  showCreateModal() {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal() {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(position: PositionListDTO) {
    this.selectedPosition.set(position);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal() {
    this.isUpdateModalVisible.set(false);
    this.selectedPosition.set(null);
  }

  confirmDelete(position: PositionListDTO) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar la posición ${position.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(position.publicId);
      }
    });
  }
}
