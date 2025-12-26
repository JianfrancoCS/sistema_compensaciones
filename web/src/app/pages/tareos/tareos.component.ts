import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TareoStore } from '@core/store/tareo.store';
import { SubsidiaryStore } from '@core/store/subsidiary.store';
import { LaborStore } from '@core/store/labor.store';
import { ButtonModule } from 'primeng/button';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TabsModule } from 'primeng/tabs';
import { ConfirmationService } from 'primeng/api';
import { TareoService } from '@core/services/tareo.service';
import { TareoListDTO, TareoDailyDTO } from '@shared/types/tareo';
import { DateDisplayComponent } from '@shared/dummys';
import { Router } from '@angular/router';

@Component({
  selector: 'app-tareos',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    TableModule,
    InputTextModule,
    DatePickerModule,
    SelectModule,
    ToastModule,
    TooltipModule,
    ConfirmDialogModule,
    TabsModule,
    DateDisplayComponent
  ],
  templateUrl: './tareos.component.html',
  styleUrl: './tareos.component.css'
})
export class TareosComponent implements OnInit {
  readonly tareoStore = inject(TareoStore);
  readonly subsidiaryStore = inject(SubsidiaryStore);
  readonly laborStore = inject(LaborStore);
  private confirmationService = inject(ConfirmationService);
  private router = inject(Router);

  activeTabIndex = signal('0');

  selectedLabor: string | null = null;
  selectedSubsidiary: string | null = null;
  createdBySearch: string = '';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;
  selectedIsProcessed: boolean | null = null;

  dailySelectedLabor: string | null = null;
  dailySelectedSubsidiary: string | null = null;
  dailyDateFrom: Date | null = null;
  dailyDateTo: Date | null = null;
  selectedIsCalculated: boolean | null = null;

  subsidiaryOptions = computed(() => {
    return this.subsidiaryStore.subsidiaries().map(s => ({
      label: s.name,
      value: s.publicId
    }));
  });

  laborOptions = computed(() => {
    return this.laborStore.selectOptions().map(l => ({
      label: l.name,
      value: l.publicId
    }));
  });

  isProcessedOptions = [
    { label: 'Todos', value: null },
    { label: 'Procesado', value: true },
    { label: 'No Procesado', value: false }
  ];

  isCalculatedOptions = [
    { label: 'Todos', value: null },
    { label: 'Calculado', value: true },
    { label: 'No Calculado', value: false }
  ];

  ngOnInit(): void {
    this.tareoStore.init();
    this.subsidiaryStore.init();
    this.laborStore.init();
  }

  loadTareos(event: TableLazyLoadEvent): void {
    if (event.first !== undefined && event.rows !== null && event.rows !== undefined) {
      const page = Math.floor(event.first / event.rows);
      if (page !== this.tareoStore.page()) {
        this.tareoStore.setPage(page);
      }
    }
    
    if (event.sortField) {
      const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';
      let sortBy = event.sortField;
      if (sortBy === 'laborName') sortBy = 'labor.name';
      else if (sortBy === 'loteName') sortBy = 'lote.name';
      else if (sortBy === 'loteSubsidiaryName') sortBy = 'lote.subsidiary.name';
      else if (sortBy === 'createdAt') sortBy = 'createdAt';
      else sortBy = 'createdAt';
      
      this.tareoStore.setSort(sortBy, sortDirection);
    }
  }

  onLaborFilterChange(value: string | null): void {
    this.selectedLabor = value;
    this.tareoStore.setLaborFilter(value);
  }

  onSubsidiaryFilterChange(value: string | null): void {
    this.selectedSubsidiary = value;
    this.tareoStore.setSubsidiaryFilter(value);
  }

  onCreatedByChange(): void {
    this.tareoStore.setCreatedByFilter(this.createdBySearch || null);
  }

  onDateFromChange(): void {
    const dateStr = this.dateFrom ? this.dateFrom.toISOString().split('T')[0] : null;
    this.tareoStore.setDateFromFilter(dateStr);
  }

  onDateToChange(): void {
    const dateStr = this.dateTo ? this.dateTo.toISOString().split('T')[0] : null;
    this.tareoStore.setDateToFilter(dateStr);
  }

  onIsProcessedChange(value: boolean | null): void {
    this.selectedIsProcessed = value;
    this.tareoStore.setIsProcessedFilter(value);
  }

  clearFilters(): void {
    this.selectedLabor = null;
    this.selectedSubsidiary = null;
    this.createdBySearch = '';
    this.dateFrom = null;
    this.dateTo = null;
    this.selectedIsProcessed = null;
    this.tareoStore.clearFilters();
  }

  confirmDelete(tareo: TareoListDTO): void {
    const loteInfo = tareo.loteName ? ` - ${tareo.loteName}` : '';
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar el tareo de ${tareo.laborName || 'N/A'}${loteInfo}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.tareoStore.deleteTareo(tareo.publicId);
      }
    });
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-PE', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  }

  onTabChange(): void {
    if (this.activeTabIndex() === '1') {
      this.tareoStore.initDaily();
    }
  }

  loadDailyTareos(event: TableLazyLoadEvent): void {
    if (event.first !== undefined && event.rows !== null && event.rows !== undefined) {
      const page = Math.floor(event.first / event.rows);
      if (page !== this.tareoStore.dailyPage()) {
        this.tareoStore.setDailyPage(page);
      }
    }
  }

  onDailyLaborFilterChange(value: string | null): void {
    this.dailySelectedLabor = value;
    this.tareoStore.setDailyLaborFilter(value);
  }

  onDailySubsidiaryFilterChange(value: string | null): void {
    this.dailySelectedSubsidiary = value;
    this.tareoStore.setDailySubsidiaryFilter(value);
  }

  onDailyDateFromChange(): void {
    const dateStr = this.dailyDateFrom ? this.dailyDateFrom.toISOString().split('T')[0] : null;
    this.tareoStore.setDailyDateFromFilter(dateStr);
  }

  onDailyDateToChange(): void {
    const dateStr = this.dailyDateTo ? this.dailyDateTo.toISOString().split('T')[0] : null;
    this.tareoStore.setDailyDateToFilter(dateStr);
  }

  onIsCalculatedChange(value: boolean | null): void {
    this.selectedIsCalculated = value;
    this.tareoStore.setIsCalculatedFilter(value);
  }

  clearDailyFilters(): void {
    this.dailySelectedLabor = null;
    this.dailySelectedSubsidiary = null;
    this.dailyDateFrom = null;
    this.dailyDateTo = null;
    this.selectedIsCalculated = null;
    this.tareoStore.clearDailyFilters();
  }

  viewTareoDetail(tareo: TareoListDTO): void {
    this.router.navigate(['/system/tareos', tareo.publicId]);
  }
}

