import { Component, inject, signal } from '@angular/core';
import { ContainerStore } from '../../core/store/container.store';
import { Container } from '@shared/types/container';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { ContainerCreateModal } from './components/create-modal/create-modal';
import { ContainerUpdateModal } from './components/update-modal/update-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-containers',
  templateUrl: './containers.html',
  styleUrls: ['./containers.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    ContainerCreateModal,
    ContainerUpdateModal,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule
  ],
  providers: [ConfirmationService]
})
export class Containers {
  protected store = inject(ContainerStore);
  private confirmationService = inject(ConfirmationService);

  readonly containers = this.store.containers;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  isCreateModalVisible = signal(false);
  isUpdateModalVisible = signal(false);
  selectedContainer = signal<Container | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.store.search(inputElement.value);
  }

  loadContainers(event: TableLazyLoadEvent): void {
    this.store.onLazyLoad(event);
  }

  showCreateModal() {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal() {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(container: Container) {
    this.selectedContainer.set(container);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal() {
    this.isUpdateModalVisible.set(false);
    this.selectedContainer.set(null);
  }

  confirmDelete(container: Container) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el contenedor ${container.displayName}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(container.publicId);
      }
    });
  }
}

