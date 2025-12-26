import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { QrRollStore } from '@core/store/qr-roll.store';
import { QrRollListDTO, CreateQrRollRequest } from '@shared/types/qr-roll';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { RippleModule } from 'primeng/ripple';
import { PaginatorModule } from 'primeng/paginator';
import { SkeletonModule } from 'primeng/skeleton';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { BatchGenerateModalComponent } from './components/batch-generate-modal/batch-generate-modal.component';
import { DateDisplayComponent } from '@shared/dummys/date-display.component';
import { SelectModule } from 'primeng/select';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TooltipModule } from 'primeng/tooltip';
import { EditQrRollModalComponent } from './components/edit-modal/edit-modal.component';
import { QrGenerateCodesModalComponent } from './components/generate-codes-modal/generate-codes-modal.component';
import {Skeleton} from 'primeng/skeleton';

@Component({
  selector: 'app-qr',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    ToastModule,
    ConfirmDialogModule,
    RippleModule,
    PaginatorModule,
    SkeletonModule,
    DialogModule,
    InputNumberModule,
    BatchGenerateModalComponent,
    DateDisplayComponent,
    SelectModule,
    SelectButtonModule,
    TooltipModule,
    EditQrRollModalComponent,
    QrGenerateCodesModalComponent,
    Skeleton,
  ],
  templateUrl: './qr.html',
  styleUrl: './qr.css'
})
export class QrComponent {
  protected store = inject(QrRollStore);
  private router = inject(Router);

  maxQrCodesPerDay: number = 1;

  readonly batchGenerateModalVisible = signal(false);
  readonly generateCodesModalVisible = signal(false); 
  readonly selectedRollForCodeGeneration = signal<string | null>(null);

  filterOptions: any[];
  sortOptions: any[];
  selectedSort: 'desc' | 'asc';
  selectedFilterValue: boolean | undefined;

  constructor() {
    this.store.resetFilters();
    this.store.init();

    const currentFilters = this.store.filters();
    this.selectedSort = currentFilters.sortDirection!;
    this.selectedFilterValue = currentFilters.hasUnprintedCodes;

    this.filterOptions = [
      { label: 'Todos', value: undefined },
      { label: 'Por Imprimir', value: true }
    ];
    this.sortOptions = [
        { label: 'Más Recientes', value: 'desc' },
        { label: 'Más Antiguos', value: 'asc' }
    ];
  }

  onPageChange(event: any): void {
    this.store.loadPage({
      page: event.page,
      size: event.rows,
      sortBy: 'createdAt',
      sortDirection: this.selectedSort
    });
  }

  onSortChange(event: any): void {
    this.selectedSort = event.value;
    this.store.setFilters({ sortBy: 'createdAt', sortDirection: this.selectedSort });
  }

  onFilterChange(event: any): void {
    this.selectedFilterValue = event.value;
    this.store.setFilters({ hasUnprintedCodes: this.selectedFilterValue });
  }

  create(request: CreateQrRollRequest): void {
    this.store.create(request);
  }

  viewQrCodes(qrRoll: QrRollListDTO): void {
    this.router.navigate(['/system/qr', qrRoll.publicId]);
  }

  showBatchGenerateModal(): void {
    this.batchGenerateModalVisible.set(true);
  }

  hideBatchGenerateModal(): void {
    this.batchGenerateModalVisible.set(false);
  }

  showGenerateCodesModal(rollPublicId: string): void {
    this.selectedRollForCodeGeneration.set(rollPublicId);
    this.generateCodesModalVisible.set(true);
  }

  hideGenerateCodesModal(): void {
    this.generateCodesModalVisible.set(false);
    this.selectedRollForCodeGeneration.set(null);
  }
}
