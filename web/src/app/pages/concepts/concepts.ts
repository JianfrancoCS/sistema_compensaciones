import { Component, inject, signal, OnInit, effect, computed } from '@angular/core';
import { ConceptStore, ConceptListDTO } from '../../core/store/concept.store';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { ConceptCreateModal } from './components/create-concept-modal/create-concept-modal';
import { ConceptUpdateModal } from './components/update-concept-modal/update-concept-modal';
import { DateDisplayComponent } from '../../shared/dummys';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { SelectModule } from 'primeng/select';

@Component({
  selector: 'app-concepts',
  templateUrl: './concepts.html',
  styleUrls: ['./concepts.css'],
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule,
    FormsModule,
    ConceptCreateModal,
    ConceptUpdateModal,
    DateDisplayComponent,
    InputGroupModule,
    InputGroupAddonModule,
    SelectModule
  ],
  providers: [ConfirmationService]
})
export class Concepts implements OnInit {
  protected store = inject(ConceptStore);
  private confirmationService = inject(ConfirmationService);

  readonly concepts = this.store.concepts;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;
  readonly categories = this.store.categories;

  readonly isCreateModalVisible = signal(false);
  readonly isUpdateModalVisible = signal(false);
  readonly selectedConcept = signal<ConceptListDTO | null>(null);
  
  selectedCategory: string | null = null;

  categoryOptions = computed(() => {
    return this.store.categories().map(c => ({
      label: c.name,
      value: c.publicId
    }));
  });

  constructor() {
    this.store.resetFilters();
    this.store.loadCategories();
    
    effect(() => {
      const categoryPublicId = this.store.filters().categoryPublicId;
      this.selectedCategory = categoryPublicId || null;
    });
  }

  ngOnInit() {
  }

  onCategoryChange(value: string | null) {
    if (value) {
      this.store.updateCategoryFilter(value);
    } else {
      this.store.clearCategoryFilter();
    }
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.store.search(query);
  }

  loadConcepts(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const pageSize = event.rows ?? 10;
    const sortBy = Array.isArray(event.sortField)
      ? event.sortField[0]
      : event.sortField || 'createdAt';
    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    this.store.loadPage({ page, pageSize, sortBy, sortDirection });
  }

  showCreateModal(): void {
    this.isCreateModalVisible.set(true);
  }

  hideCreateModal(): void {
    this.isCreateModalVisible.set(false);
  }

  showUpdateModal(concept: ConceptListDTO): void {
    this.selectedConcept.set(concept);
    this.isUpdateModalVisible.set(true);
  }

  hideUpdateModal(): void {
    this.isUpdateModalVisible.set(false);
    this.selectedConcept.set(null);
  }

  confirmDelete(concept: ConceptListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el concepto ${concept.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(concept.publicId);
      }
    });
  }
}

