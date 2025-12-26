import { Component, Input, Output, EventEmitter, inject, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { QrRollStore } from '@core/store/qr-roll.store';
import { QrRollListDTO, UpdateQrRollRequest } from '@shared/types/qr-roll';

@Component({
  selector: 'app-edit-qr-roll-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DialogModule,
    ButtonModule,
    InputNumberModule
  ],
  templateUrl: './edit-modal.component.html',
})
export class EditQrRollModalComponent implements OnChanges {
  protected store = inject(QrRollStore);

  @Input() visible: boolean = false;
  @Input() qrRoll: QrRollListDTO | null = null;
  @Output() onHide = new EventEmitter<void>();

  maxQrCodesPerDay: number = 1;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['qrRoll'] && this.qrRoll) {
      this.maxQrCodesPerDay = this.qrRoll.maxQrCodesPerDay;
    }
  }

  hideModal(): void {
    this.onHide.emit();
  }

  save(form: NgForm): void {
    if (form.valid && this.qrRoll) {
      const request: UpdateQrRollRequest = {
        maxQrCodesPerDay: this.maxQrCodesPerDay
      };
      this.store.update({ publicId: this.qrRoll.publicId, request });
    }
  }
}
