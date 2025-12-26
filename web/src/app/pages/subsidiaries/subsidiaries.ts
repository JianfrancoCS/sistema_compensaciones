import { Component, inject, signal } from '@angular/core';
import { SubsidiaryStore } from '../../core/store/subsidiary.store';
import { SubsidiaryListDTO } from '@shared/types/subsidiary';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { SubsidiaryCreateModal } from './components/create-modal/create-modal';
import { SubsidiaryUpdateModal } from './components/update-modal/update-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-subsidiaries',
  templateUrl: './subsidiaries.html',
  styleUrls: ['./subsidiaries.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    SubsidiaryCreateModal,
    SubsidiaryUpdateModal,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule
  ]
})
export class Subsidiaries {
  protected store = inject(SubsidiaryStore);
  private confirmationService = inject(ConfirmationService);

  readonly subsidiaries = this.store.subsidiaries;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  readonly isCreateModalVisible = signal(false);
  readonly isUpdateModalVisible = signal(false);
  readonly selectedSubsidiary = signal<SubsidiaryListDTO | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.store.search(query);
  }


  loadSubsidiaries(event: TableLazyLoadEvent): void {
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

  showUpdateModal(subsidiary: SubsidiaryListDTO): void {
    this.selectedSubsidiary.set(subsidiary);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal(): void {
    this.isUpdateModalVisible.set(false);
    this.selectedSubsidiary.set(null);
  }

  confirmDelete(subsidiary: SubsidiaryListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el fundo ${subsidiary.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(subsidiary.publicId);
      }
    });
  }

}
