import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PayslipStore } from '@core/store/payslip.store';
import { PayslipListDTO } from '@shared/types/payslip';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { DatePickerModule } from 'primeng/datepicker';
import { Router } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-payslips',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    ProgressSpinnerModule,
    TableModule,
    DatePickerModule,
    CurrencyPipe,
    ToastModule,
    DatePipe,
  ],
  templateUrl: './payslips.component.html',
})
export class PayslipsComponent implements OnInit {
  protected store = inject(PayslipStore);
  private router = inject(Router);

  readonly payslips = this.store.payslips;
  readonly loading = this.store.loading;
  readonly totalRecords = this.store.totalElements;
  readonly isEmpty = this.store.isEmpty;

  periodFrom = signal<Date | null>(null);
  periodTo = signal<Date | null>(null);

  ngOnInit() {
    const now = new Date();
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const lastDayOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    
    this.periodFrom.set(firstDayOfMonth);
    this.periodTo.set(lastDayOfMonth);
    
    const filters: any = {
      periodFrom: this.formatDate(firstDayOfMonth),
      periodTo: this.formatDate(lastDayOfMonth),
    };
    this.store.setFilters(filters);
  }

  loadPayslips(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const pageSize = event.rows ?? 10;
    const sortBy = Array.isArray(event.sortField)
      ? event.sortField[0]
      : event.sortField || 'createdAt';
    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';

    const filters: any = {
      periodFrom: this.periodFrom() ? this.formatDate(this.periodFrom()!) : null,
      periodTo: this.periodTo() ? this.formatDate(this.periodTo()!) : null,
      page,
      pageSize,
      sortBy,
      sortDirection,
    };

    this.store.setFilters(filters);
  }

  onFilterChange(): void {
    const filters: any = {
      periodFrom: this.periodFrom() ? this.formatDate(this.periodFrom()!) : null,
      periodTo: this.periodTo() ? this.formatDate(this.periodTo()!) : null,
    };
    this.store.setFilters(filters);
    const currentFilters = this.store.filters();
    this.store.loadPage({
      page: 0,
      pageSize: currentFilters.pageSize,
      sortBy: currentFilters.sortBy,
      sortDirection: currentFilters.sortDirection
    });
  }

  clearFilters(): void {
    const now = new Date();
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const lastDayOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    
    this.periodFrom.set(firstDayOfMonth);
    this.periodTo.set(lastDayOfMonth);
    
    const filters: any = {
      periodFrom: this.formatDate(firstDayOfMonth),
      periodTo: this.formatDate(lastDayOfMonth),
    };
    this.store.setFilters(filters);
    
    const currentFilters = this.store.filters();
    this.store.loadPage({
      page: 0,
      pageSize: currentFilters.pageSize,
      sortBy: currentFilters.sortBy,
      sortDirection: currentFilters.sortDirection
    });
  }

  viewPayslip(payslip: PayslipListDTO): void {
    this.router.navigate(['/system/payslips', payslip.publicId, 'view']);
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}

