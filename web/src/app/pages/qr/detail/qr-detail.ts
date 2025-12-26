import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QrRollStore } from '@core/store/qr-roll.store';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { QrCodeComponent } from 'ng-qrcode';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-qr-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    SelectModule,
    ToastModule,
    QrCodeComponent,
    RouterLink,
    TooltipModule
  ],
  templateUrl: './qr-detail.html',
  styleUrl: './qr-detail.css'
})
export class QrDetailComponent implements OnInit {
  protected store = inject(QrRollStore);
  private route = inject(ActivatedRoute);

  rollPublicId: string = '';
  isUsedFilter: boolean | undefined = undefined;
  isPrintedFilter: boolean | undefined = undefined;

  filterOptionsUsed: any[];
  filterOptionsPrinted: any[];

  constructor() {
    this.filterOptionsUsed = [
      { label: 'Usado', value: true },
      { label: 'No Usado', value: false }
    ];
    this.filterOptionsPrinted = [
      { label: 'Impreso', value: true },
      { label: 'No Impreso', value: false }
    ];
  }

  ngOnInit(): void {
    this.rollPublicId = this.route.snapshot.paramMap.get('id') || '';
    if (this.rollPublicId) {
      this.store.loadQrCodesForRoll(this.rollPublicId);
    }
  }

  onFilterChange(): void {
    this.store.setQrCodeFilters({
      isUsed: this.isUsedFilter,
      isPrinted: this.isPrintedFilter
    });
  }

  onClearFilters(): void {
    this.isUsedFilter = undefined;
    this.isPrintedFilter = undefined;
    this.store.clearQrCodeFilters();
  }
}
