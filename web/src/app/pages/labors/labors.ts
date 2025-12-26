import { Component, inject, signal } from '@angular/core';
import { LaborStore, LaborListDTO } from '@core/store/labor.store';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';
import { CreateLaborModalComponent } from './components/create-modal/create-modal.component';
import { UpdateLaborModalComponent } from './components/update-modal/update-modal.component';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { LaborUnitStore } from '@core/store/labor-unit.store';
import { RippleModule } from 'primeng/ripple';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-labors',
  standalone: true,
  imports: [
    CommonModule,
    ConfirmDialogModule,
    ToastModule,
    InputTextModule,
    ButtonModule,
    CreateLaborModalComponent,
    UpdateLaborModalComponent,
    ToggleSwitchModule,
    FormsModule,
    SelectModule,
    RippleModule,
    TableModule,
    InputGroupModule,
    InputGroupAddonModule
  ],
  templateUrl: './labors.html',
  styleUrl: './labors.css'
})
export class LaborsComponent {
  protected store = inject(LaborStore);
  protected laborUnitStore = inject(LaborUnitStore);

  readonly labors = this.store.labors;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  readonly createModalVisible = signal(false);
  readonly updateModalVisible = signal(false);
  readonly selectedLabor = signal<LaborListDTO | null>(null);

  public nameFilter: string = '';
  public isPieceworkFilter: boolean | undefined = undefined;
  public laborUnitFilter: string | undefined = undefined;

  constructor() {
    this.store.resetFilters();
    this.store.init();
    this.laborUnitStore.init();
  }

  onLazyLoad(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const size = event.rows ?? 10;
    const sortBy = Array.isArray(event.sortField)
      ? event.sortField[0]
      : event.sortField || 'name';
    const sortDirection = event.sortOrder === 1 ? 'asc' : 'desc';

    this.store.loadPage({ page, size, sortBy, sortDirection });
  }

  onFilterByName(event: Event): void {
    this.nameFilter = (event.target as HTMLInputElement).value;
    this.onFilter();
  }

  onClearNameFilter(): void {
    this.nameFilter = '';
    this.onFilter();
  }

  onFilter(): void {
    this.store.setFilters({
      ...this.store.filters(),
      name: this.nameFilter,
      isPiecework: this.isPieceworkFilter,
      laborUnitPublicId: this.laborUnitFilter,
      page: 0,
    });
  }

  onClearFilter(): void {
    this.nameFilter = '';
    this.isPieceworkFilter = undefined;
    this.laborUnitFilter = undefined;
    this.store.setFilters({
      name: '',
      isPiecework: undefined,
      laborUnitPublicId: undefined,
      page: 0,
      size: this.store.filters().size,
      sortBy: 'name',
      sortDirection: 'asc',
    });
  }

  showCreateModal(): void {
    this.createModalVisible.set(true);
  }

  hideCreateModal(): void {
    this.createModalVisible.set(false);
  }

  showUpdateModal(labor: LaborListDTO): void {
    this.selectedLabor.set(labor);
    this.updateModalVisible.set(true);
  }

  hideUpdateModal(): void {
    this.updateModalVisible.set(false);
    this.selectedLabor.set(null);
  }

  confirmDelete(labor: LaborListDTO): void {
    this.store.confirmDelete(labor);
  }
}
