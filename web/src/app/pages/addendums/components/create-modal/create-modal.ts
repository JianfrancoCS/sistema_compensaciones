import { Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AddendumService } from '@core/services/addendum.service';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectOption } from '@core/models/api.model';
import { ContractService } from '@core/services/contract.service';
import { AddendumTypeService } from '@core/services/addendum-type.service';
import { AddendumTemplateService } from '@core/services/addendum-template.service';

@Component({
  selector: 'app-addendum-create-modal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    InputTextModule,
    SelectModule,
    DatePickerModule
  ],
  templateUrl: './create-modal.html',
  styleUrls: ['./create-modal.css']
})
export class AddendumCreateModal implements OnInit {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private addendumService = inject(AddendumService);
  private contractService = inject(ContractService);
  private addendumTypeService = inject(AddendumTypeService);
  private addendumTemplateService = inject(AddendumTemplateService);
  private messageService = inject(MessageService);

  contracts = signal<SelectOption[]>([]);
  addendumTypes = signal<SelectOption[]>([]);
  addendumTemplates = signal<SelectOption[]>([]);

  addendumForm = this.fb.group({
    contractPublicId: ['', Validators.required],
    addendumTypePublicId: ['', Validators.required],
    templatePublicId: ['', Validators.required],
    startDate: [new Date(), Validators.required],
    endDate: [<Date | null>null]
  });

  ngOnInit(): void {
    this.loadContracts();
    this.loadAddendumTypes();
    this.onAddendumTypeChange();
  }

  loadContracts() {
    this.contractService.getContractSelectOptions().subscribe(response => {
      if (response.success) {
        this.contracts.set(response.data);
      }
    });
  }

  loadAddendumTypes() {
    this.addendumTypeService.getAddendumTypeSelectOptions().subscribe(response => {
      if (response.success) {
        this.addendumTypes.set(response.data);
      }
    });
  }

  onAddendumTypeChange(): void {
    this.addendumForm.get('addendumTypePublicId')?.valueChanges.subscribe(typeId => {
      this.addendumForm.get('templatePublicId')?.reset('');
      this.addendumTemplates.set([]);
      if (typeId) {
        this.addendumTemplateService.getSelectOptions(typeId).subscribe(response => {
          if (response.success) {
            this.addendumTemplates.set(response.data);
          }
        });
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.addendumForm.reset({ startDate: new Date() });
  }

  createAddendum() {
    if (this.addendumForm.invalid) {
      this.addendumForm.markAllAsTouched();
      return;
    }

    const formValue = this.addendumForm.value;

    this.addendumService.create({
      contractPublicId: formValue.contractPublicId!,
      addendumTypePublicId: formValue.addendumTypePublicId!,
      templatePublicId: formValue.templatePublicId!,
      startDate: formValue.startDate!.toISOString().split('T')[0],
      endDate: formValue.endDate ? formValue.endDate.toISOString().split('T')[0] : undefined,
      variables: []
    }).subscribe({
      next: (response) => {
        this.messageService.add({ severity: response.success ? 'success' : 'error', summary: response.success ? 'Éxito' : 'Error', detail: response.message });
        if (response.success) {
          this.hideModal();
        }
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.message || 'Ocurrió un error al crear la adenda.' });
      }
    });
  }
}
