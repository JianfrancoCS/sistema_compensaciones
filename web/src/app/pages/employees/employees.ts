import { Component, computed, inject, signal, effect } from '@angular/core';
import { Router } from '@angular/router';
import { EmployeeStore } from '../../core/store/employee.store';
import { EmployeeListDTO } from '@shared/types/employee';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { CommonModule } from '@angular/common';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { ToastModule } from 'primeng/toast';
import { MessageService, ConfirmationService } from 'primeng/api';
import { EmployeeCreateModal } from './components/create-modal/create-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';

@Component({
  selector: 'app-employees',
  templateUrl: './employees.html',
  styleUrls: ['./employees.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    SelectModule,
    CommonModule,
    IconFieldModule,
    InputIconModule,
    ToastModule,
    EmployeeCreateModal,
    DateDisplayComponent,
    ConfirmDialogModule,
    InputGroupModule,
    InputGroupAddonModule
  ],
  providers: [MessageService, ConfirmationService] // Add ConfirmationService
})
export class Employees {
  protected store = inject(EmployeeStore);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private router = inject(Router);

  readonly employees = this.store.employees;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  showCreateModal = signal(false);

  nationalityOptions = [
    { label: 'Todos los empleados', value: null },
    { label: 'Solo peruanos', value: true },
    { label: 'Solo extranjeros', value: false }
  ];

  subsidiaryOptions = computed(() => {
    return this.store.subsidiarySelectOptions().map(option => ({
      label: option.name,
      value: option.publicId
    }));
  });

  positionOptions = computed(() => {
    return this.store.positionSelectOptions().map(option => ({
      label: option.name,
      value: option.publicId
    }));
  });

  constructor() {
    this.store.resetFilters();
    this.store.init(); // Initialize the store
    effect(() => {
      const error = this.store.error(); // Get error from store
      if (error) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: error });
        this.store.clearError(); // Clear error after displaying
      }
    });
  }

  onSearch(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    this.store.search(inputElement.value); // Call store's search method
  }

  loadEmployees(event: TableLazyLoadEvent) {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const pageSize = event.rows ?? 10;

    let sortBy = 'createdAt';
    if (event.sortField) {
      if (Array.isArray(event.sortField)) {
        sortBy = event.sortField[0] || 'createdAt';
      } else {
        sortBy = event.sortField;
      }
    }

    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    this.store.loadPage({ page, pageSize, sortBy, sortDirection }); // Call store's loadPage method
  }

  showCreatePage() {
    this.showCreateModal.set(true);
  }

  hideCreateModal() {
    this.showCreateModal.set(false);
    this.store.refresh(); // Refresh the list after creating
  }

  confirmDelete(employee: EmployeeListDTO) {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar al empleado ${employee.names} ${employee.paternalLastname}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(employee.publicId);
        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: 'Empleado eliminado correctamente'
        });
      }
    });
  }

  onNationalityFilter(isNational: boolean | null) {
    this.store.filterByNationality(isNational);
  }

  onSubsidiaryFilter(subsidiaryPublicId: string | null) {
    this.store.updateFilter({ subsidiaryPublicId });
  }

  onPositionFilter(positionPublicId: string | null) {
    this.store.updateFilter({ positionPublicId });
  }

  navigateToForeignPersons() {
    this.router.navigate(['/foreign-persons']);
  }
}
