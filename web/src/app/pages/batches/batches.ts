import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService } from 'primeng/api'; // Solo MessageService, ConfirmationService se inyecta en el store
import { BatchStore } from '@core/store/batches.store';
import { BatchCreateModalComponent } from './components/batch-create-modal/batch-create-modal.component';
import { BatchUpdateModalComponent } from './components/batch-update-modal/batch-update-modal.component';
import { TableLazyLoadEvent } from 'primeng/table';
import { BatchListDTO, BatchDetailsDTO } from '@shared/types/batches';
import {DateDisplayComponent} from '@shared/dummys';

@Component({
  selector: 'app-batches',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    InputGroupModule,
    InputGroupAddonModule,
    ToastModule,
    ConfirmDialogModule,
    DateDisplayComponent,
    BatchCreateModalComponent,
    BatchUpdateModalComponent
  ],
  providers: [MessageService], // Solo MessageService aquí
  templateUrl: './batches.html',
  styleUrl: './batches.css'
})
export class BatchesComponent implements OnInit {
  store = inject(BatchStore);

  ngOnInit(): void {
    this.store.resetFilters();
    this.store.init();
  }

  loadBatches(event: TableLazyLoadEvent) {
    const page = event.first! / event.rows!;
    const pageSize = event.rows!;
    const sortBy = event.sortField as string || 'createdAt';
    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    this.store.loadPage({ page, pageSize, sortBy, sortDirection });
  }

  onSearch(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    this.store.updateFilter(inputElement.value);
  }

  showCreateModal() {
    this.store.showCreateModal();
  }

  hideCreateModal() {
    this.store.hideCreateModal();
  }

  showUpdateModal(batch: BatchListDTO) {
    this.store.getDetails(batch.publicId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.store.showUpdateModal(response.data);
        }
      },
      error: (err) => {
        console.error('Error al obtener detalles del lote en el componente (manejado por el store):', err);
      }
    });
  }

  hideUpdateModal() {
    this.store.hideUpdateModal();
  }

  confirmDelete(batch: BatchListDTO) {
    this.store.confirmAndDelete(batch); // Llamada al método del store
  }
}
