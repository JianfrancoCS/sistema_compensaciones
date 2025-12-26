import { Component, inject, signal } from '@angular/core';
import { JustificationStore, JustificationListDTO } from '@core/store/justification.store';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { TagModule } from 'primeng/tag';
import { CreateJustificationModalComponent } from './components/create-modal/create-modal';
import { UpdateJustificationModalComponent } from './components/update-modal/update-modal';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { DateDisplayComponent } from '@shared/dummys';

@Component({
  selector: 'app-justifications',
  templateUrl: './justifications.html',
  styleUrls: ['./justifications.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    InputGroupModule,
    InputGroupAddonModule,
    TagModule,
    CreateJustificationModalComponent,
    UpdateJustificationModalComponent,
    IconFieldModule,
    InputIconModule,
    DateDisplayComponent
  ]
})
export class JustificationsComponent {
  protected store = inject(JustificationStore);
  private confirmationService = inject(ConfirmationService);

  readonly justifications = this.store.justifications;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  readonly isCreateModalVisible = signal(false);
  readonly isUpdateModalVisible = signal(false);
  readonly selectedJustification = signal<JustificationListDTO | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.store.search(query);
  }

  loadJustifications(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const size = event.rows ?? 10;
    const sortBy = Array.isArray(event.sortField)
      ? event.sortField[0]
      : event.sortField || 'createdAt';
    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    this.store.loadPage({ page, size, sortBy, sortDirection });
  }

  showCreateModal(): void {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal(): void {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(justification: JustificationListDTO): void {
    this.selectedJustification.set(justification);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal(): void {
    this.isUpdateModalVisible.set(false);
    this.selectedJustification.set(null);
  }

  confirmDelete(justification: JustificationListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar la justificación ${justification.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(justification.publicId);
      }
    });
  }
}
