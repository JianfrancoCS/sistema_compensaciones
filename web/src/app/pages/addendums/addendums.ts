import { Component, computed, inject, signal, effect } from '@angular/core';
import { AddendumService, AddendumListDTO } from '@core/services/addendum.service';
import { handleApiResponse, handleApiError } from '@core/utils/api-response.helper';
import { TableHelper } from '@core/utils/table-helper';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { CommonModule } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { AddendumCreateModal } from './components/create-modal/create-modal';
import { UpdateAddendumModalComponent } from './components/update-addendum-modal/update-addendum-modal';

@Component({
  selector: 'app-addendums',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    CommonModule,
    ToastModule,
    ConfirmDialogModule,
    AddendumCreateModal,
    UpdateAddendumModalComponent
  ],
  templateUrl: './addendums.html',
  styleUrls: ['./addendums.css'],
  providers: [MessageService, ConfirmationService]
})
export class Addendums {
  public addendumService = inject(AddendumService);
  private confirmationService = inject(ConfirmationService);
  private messageService = inject(MessageService);

  addendums = this.addendumService.addendums;
  totalRecords = computed(() => this.addendums().totalElements);

  isCreateModalVisible = signal(false);
  isUpdateModalVisible = signal(false);
  selectedAddendum = signal<AddendumListDTO | null>(null);

  constructor() {
    effect(() => {
      const error = this.addendumService.addendums().error;
      if (error) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: error });
      }
    });
  }

  showCreateModal() {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal() {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(addendum: AddendumListDTO) {
    this.selectedAddendum.set(addendum);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal() {
    this.isUpdateModalVisible.set(false);
    this.selectedAddendum.set(null);
  }

  onSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    this.addendumService.search(input.value);
  }

  loadAddendums(event: any) {
    const { page, pageSize, sortField, sortDirection } = TableHelper.processPaginationEvent(event);
    this.addendumService.loadAddendums(page, pageSize, sortField, sortDirection);
  }

  confirmDelete(addendum: AddendumListDTO) {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar la adenda "${addendum.addendumNumber}"?`,
      header: 'Confirmación de eliminación',
      icon: 'pi pi-info-circle',
      accept: () => {
        this.deleteAddendum(addendum.publicId);
      },
      reject: () => {
        this.messageService.add({ severity: 'info', summary: 'Cancelado', detail: 'La eliminación ha sido cancelada.' });
      }
    });
  }

  deleteAddendum(publicId: string) {
    this.addendumService.delete(publicId).subscribe({
      next: (response) => {
        handleApiResponse(response, this.messageService, 'Adenda eliminada correctamente');
      },
      error: (err) => {
        handleApiError(err, this.messageService, 'Error al eliminar la adenda');
      }
    });
  }
}
