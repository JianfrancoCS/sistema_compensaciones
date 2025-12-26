import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmationService, MessageService } from 'primeng/api';
import { SubsidiarySignerStore } from '../../core/store/subsidiary-signer.store';
import { SubsidiarySignerListDTO } from '../../core/services/subsidiary-signer.service';
import { AssignSignerCreateModalComponent } from './components/assign-signer-create-modal/assign-signer-create-modal';
import { AssignSignerUpdateModalComponent } from './components/assign-signer-update-modal/assign-signer-update-modal';

@Component({
  selector: 'app-subsidiary-signers',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    ToastModule,
    ConfirmDialogModule,
    InputTextModule,
    InputGroupModule,
    InputGroupAddonModule,
    TooltipModule,
    AssignSignerCreateModalComponent,
    AssignSignerUpdateModalComponent
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './subsidiary-signers.html',
  styleUrls: ['./subsidiary-signers.css']
})
export class SubsidiarySignersComponent implements OnInit, OnDestroy {
  protected store = inject(SubsidiarySignerStore);
  private confirmationService = inject(ConfirmationService);

  readonly subsidiaries = this.store.subsidiaries;
  readonly loading = this.store.loading;
  readonly isEmpty = this.store.isEmpty;
  readonly totalRecords = this.store.totalElements;

  readonly isAssignModalVisible = signal(false);
  readonly isEditModalVisible = signal(false);
  readonly selectedSubsidiary = signal<SubsidiarySignerListDTO | null>(null);
  readonly selectedSignerPublicId = signal<string | null>(null);

  constructor() {
    this.store.resetFilters();
  }

  ngOnDestroy(): void {
    this.store.clearImageBlobUrls();
  }

  getImageUrl(subsidiary: SubsidiarySignerListDTO): string | null {
    return this.store.getImageBlobUrl(subsidiary.signatureImageUrl);
  }

  isImageLoading(subsidiary: SubsidiarySignerListDTO): boolean {
    return this.store.isImageLoading(subsidiary.signatureImageUrl);
  }

  ngOnInit() {
    this.store.loadSubsidiaries();
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.store.search(inputElement.value);
  }

  loadSubsidiaries(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const pageSize = event.rows ?? 10;

    let sortBy = 'subsidiaryName';
    if (event.sortField) {
      if (Array.isArray(event.sortField)) {
        sortBy = event.sortField[0] || 'subsidiaryName';
      } else {
        sortBy = event.sortField;
      }
    }

    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    this.store.loadPage({ page, pageSize, sortBy, sortDirection });
  }

  showAssignModal(subsidiary: SubsidiarySignerListDTO): void {
    this.selectedSubsidiary.set(subsidiary);
    this.selectedSignerPublicId.set(null);
    this.isAssignModalVisible.set(true);
  }

  showEditModal(subsidiary: SubsidiarySignerListDTO): void {
    if (!subsidiary.hasSigner) {
      return;
    }
    this.selectedSubsidiary.set(subsidiary);
    this.store.getSignerBySubsidiary(subsidiary.subsidiaryPublicId);
    
    this.isEditModalVisible.set(true);
  }

  hideAssignModal(): void {
    this.isAssignModalVisible.set(false);
    this.isEditModalVisible.set(false);
    this.selectedSubsidiary.set(null);
    this.selectedSignerPublicId.set(null);
  }

  confirmDelete(subsidiary: SubsidiarySignerListDTO): void {
    if (!subsidiary.hasSigner) {
      return;
    }

    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el responsable de firma del fundo ${subsidiary.subsidiaryName}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.deleteSigner(subsidiary.subsidiaryPublicId);
      }
    });
  }
}

