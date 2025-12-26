import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommandContractResponse } from '@shared/types/contract';

@Component({
  selector: 'app-contract-confirmation-modal',
  standalone: true,
  imports: [
    CommonModule,
    DialogModule,
    ButtonModule
  ],
  templateUrl: './contract-confirmation-modal.html',
  styleUrls: ['./contract-confirmation-modal.css']
})
export class ContractConfirmationModalComponent {
  @Input() visible: boolean = false;
  @Input() contract: CommandContractResponse | null = null;
  @Output() onHide = new EventEmitter<void>();
  @Output() onViewContracts = new EventEmitter<void>();
  @Output() onSign = new EventEmitter<void>();
  @Output() onContinue = new EventEmitter<void>();

  hideModal() {
    this.onHide.emit();
  }

  viewContracts() {
    this.onViewContracts.emit();
    this.hideModal();
  }

  signContract() {
    this.onSign.emit();
  }

  continueCreating() {
    this.onContinue.emit();
    this.hideModal();
  }
}

