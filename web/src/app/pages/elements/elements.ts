import { Component, inject, signal } from '@angular/core';
import { ElementStore } from '../../core/store/element.store';
import { ContainerStore } from '../../core/store/container.store';
import { Element } from '@shared/types/element';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { ConfirmationService } from 'primeng/api';
import { ElementCreateModal } from './components/create-modal/create-modal';
import { ElementUpdateModal } from './components/update-modal/update-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { SelectModule } from 'primeng/select';

@Component({
  selector: 'app-elements',
  templateUrl: './elements.html',
  styleUrls: ['./elements.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    ElementCreateModal,
    ElementUpdateModal,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule,
    SelectModule
  ],
  providers: [ConfirmationService]
})
export class Elements {
  protected store = inject(ElementStore);
  protected containerStore = inject(ContainerStore);
  private confirmationService = inject(ConfirmationService);

  readonly elements = this.store.elements;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  isCreateModalVisible = signal(false);
  isUpdateModalVisible = signal(false);
  selectedElement = signal<Element | null>(null);

  constructor() {
    this.store.resetFilters();
    this.store.init();
    if (this.containerStore.containers().length === 0) {
      this.containerStore.init();
    }
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.store.search(inputElement.value);
  }

  onContainerFilterChange(containerPublicId: string | null): void {
    this.store.filterByContainer(containerPublicId);
  }

  loadElements(event: TableLazyLoadEvent): void {
    this.store.onLazyLoad(event);
  }

  showCreateModal() {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal() {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(element: Element) {
    this.selectedElement.set(element);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal() {
    this.isUpdateModalVisible.set(false);
    this.selectedElement.set(null);
  }

  confirmDelete(element: Element) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el elemento ${element.displayName}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(element.publicId);
      }
    });
  }
}

