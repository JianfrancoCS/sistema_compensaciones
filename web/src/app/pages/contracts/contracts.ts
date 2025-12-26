import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { ContractStore } from '@core/store/contracts.store';
import { ContractListDTO } from '@shared/types/contract';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { CommonModule } from '@angular/common';
import { SelectModule } from 'primeng/select';
import { SelectOption } from '@shared/types/api';
import { StateSelectOptionDTO } from '@shared/types/state';
import { TooltipModule } from 'primeng/tooltip';
import { UploadContractModalComponent } from './components/upload-contract-modal/upload-contract-modal';
import { ViewContractContentModalComponent } from './components/view-contract-content-modal/view-contract-content-modal';
import { ViewContractDetailsModalComponent } from './components/view-contract-details-modal/view-contract-details-modal';
import { SignContractModalComponent } from './components/sign-contract-modal/sign-contract-modal';
import { TableLazyLoadEvent } from 'primeng/table';
import { ToastModule } from 'primeng/toast'; // Re-import ToastModule

@Component({
  selector: 'app-contracts',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    InputGroupModule,
    InputGroupAddonModule,
    CommonModule,
    SelectModule,
    TooltipModule,
    UploadContractModalComponent,
    ViewContractContentModalComponent,
    ViewContractDetailsModalComponent,
    SignContractModalComponent,
    ToastModule // Add ToastModule back to imports
  ],
  templateUrl: './contracts.html',
  styleUrls: ['./contracts.css'],
  providers: []
})
export class Contracts implements OnInit {
  protected contractStore = inject(ContractStore);

  contracts = this.contractStore.contracts;
  loading = this.contractStore.loading;
  totalRecords = this.contractStore.totalElements;
  isEmpty = this.contractStore.isEmpty;
  error = this.contractStore.error;

  selectedContract = signal<ContractListDTO | null>(null);
  isUploadModalVisible = signal(false);
  isViewModalVisible = signal(false);
  isDetailsModalVisible = signal(false);
  isSignModalVisible = signal(false);

  contractTypes = signal<SelectOption[]>([]);
  states = signal<StateSelectOptionDTO[]>([]);

  constructor() {
  }

  ngOnInit() {
    this.contractStore.resetFilters();
    this.contractStore.init();
    this.loadFilterOptions();
  }

  loadFilterOptions() {
    this.contractStore.getContractTypeSelectOptions().subscribe(res => {
      if (res.success) {
        this.contractTypes.set(res.data);
      }
    });

    this.contractStore.getStatesSelectOptions().subscribe(res => {
      if (res.success) {
        this.states.set(res.data);
      }
    });
  }

  onSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    this.contractStore.search(input.value);
  }

  onFilterByContractType(typeId: string) {
    this.contractStore.filterByContractType(typeId);
  }

  onFilterByState(stateId: string) {
    this.contractStore.filterByState(stateId);
  }

  loadContracts(event: TableLazyLoadEvent) {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const pageSize = event.rows ?? 10;
    const sortBy = event.sortField ? (Array.isArray(event.sortField) ? event.sortField[0] : event.sortField) : 'updatedAt';
    const sortDirection = (event.sortOrder === 1) ? 'ASC' : 'DESC';

    this.contractStore.loadPage({ page, pageSize, sortBy, sortDirection });
  }

  navigateToCreate() {
    this.contractStore.navigateToCreateContract();
  }

  editContract(contract: ContractListDTO) {
    this.contractStore.navigateToEditContract(contract.publicId);
  }

  viewContract(contract: ContractListDTO) {
    this.selectedContract.set(contract);
    this.isViewModalVisible.set(true);
  }

  hideViewModal() {
    this.isViewModalVisible.set(false);
    this.selectedContract.set(null);
  }

  showUploadModal(contract: ContractListDTO) {
    this.selectedContract.set(contract);
    this.isUploadModalVisible.set(true);
  }

  hideUploadModal() {
    this.isUploadModalVisible.set(false);
    this.selectedContract.set(null);
  }

  viewContractDetails(contract: ContractListDTO) {
    this.selectedContract.set(contract);
    this.isDetailsModalVisible.set(true);
  }

  hideDetailsModal() {
    this.isDetailsModalVisible.set(false);
    this.selectedContract.set(null);
  }

  showSignModal(contract: ContractListDTO) {
    this.selectedContract.set(contract);
    this.isSignModalVisible.set(true);
  }

  hideSignModal() {
    this.isSignModalVisible.set(false);
    this.selectedContract.set(null);
  }

  onContractSigned() {
    this.hideSignModal();
    this.contractStore.loadPage({
      page: 0,
      pageSize: 10,
      sortBy: 'updatedAt',
      sortDirection: 'DESC'
    });
  }

  cancelContract(contract: ContractListDTO) {
    if (this.isCancelled(contract)) {
      return; // No permitir anular si ya está anulado
    }
    const cancelRequest = {
      statePublicId: 'CANCELLED_STATE_ID' // This ID should come from configuration
    };
    this.contractStore.cancelContract(contract.publicId, cancelRequest).subscribe();
  }

  isCancelled(contract: ContractListDTO): boolean {
    const stateName = contract.stateName?.toLowerCase() || '';
    return stateName === 'anulado' || stateName.includes('cancelado') || stateName.includes('cancelled');
  }
}
