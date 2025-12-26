import { Component, EventEmitter, inject, Input, OnChanges, OnInit, Output, signal, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { AddendumService, UpdateAddendumRequest, AddendumVariableValuePayload, AddendumListDTO } from '@core/services/addendum.service';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select'; // Changed from DropdownModule
import { SelectOption, StateSelectOptionDTO } from '@core/models/api.model';
import { AddendumTypeService } from '@core/services/addendum-type.service';
import { AddendumTemplateService } from '@core/services/addendum-template.service';

@Component({
  selector: 'app-update-addendum-modal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    InputTextModule,
    SelectModule // Changed from DropdownModule
  ],
  templateUrl: './update-addendum-modal.html',
  styleUrls: ['./update-addendum-modal.css']
})
export class UpdateAddendumModalComponent implements OnInit, OnChanges {
  @Input() visible: boolean = false;
  @Input() addendum: AddendumListDTO | null = null;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private addendumService = inject(AddendumService);
  private addendumTypeService = inject(AddendumTypeService);
  private addendumTemplateService = inject(AddendumTemplateService);
  private messageService = inject(MessageService);

  addendumTypes = signal<SelectOption[]>([]);
  templates = signal<SelectOption[]>([]);
  states = signal<StateSelectOptionDTO[]>([]);

  addendumForm = this.fb.group({
    addendumTypePublicId: ['', Validators.required],
    statePublicId: ['', Validators.required],
    templatePublicId: ['', Validators.required],
    variables: this.fb.group({})
  });

  ngOnInit(): void {
    this.loadDropdownData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.visible && this.addendum) {
      this.addendumService.getAddendumForCommand(this.addendum.publicId).subscribe(res => {
        if (res.success) {
          const data = res.data;
          this.addendumForm.patchValue({
            addendumTypePublicId: data.addendumTypePublicId,
            statePublicId: data.statePublicId,
            templatePublicId: data.templatePublicId
          });
          this.addendumTemplateService.getSelectOptions(data.addendumTypePublicId).subscribe(templateRes => this.templates.set(templateRes.data));
        }
      });
    } else {
        this.addendumForm.reset();
    }
  }

  loadDropdownData() {
    this.addendumTypeService.getAddendumTypeSelectOptions().subscribe(res => this.addendumTypes.set(res.data));
    this.addendumService.getStatesSelectOptions().subscribe(res => this.states.set(res.data));
  }

  hideModal() {
    this.onHide.emit();
    this.addendumForm.reset();
  }

  updateAddendum() {
    if (this.addendumForm.invalid || !this.addendum) {
      this.addendumForm.markAllAsTouched();
      return;
    }

    const formValue = this.addendumForm.getRawValue();
    const variablesPayload: AddendumVariableValuePayload[] = Object.entries(formValue.variables).map(([code, value]) => ({
      code,
      value: String(value)
    }));

    const request: UpdateAddendumRequest = {
      addendumTypePublicId: formValue.addendumTypePublicId!,
      statePublicId: formValue.statePublicId!,
      templatePublicId: formValue.templatePublicId!,
      variables: variablesPayload
    };

    this.addendumService.update(this.addendum.publicId, request).subscribe({
      next: (response) => {
        this.messageService.add({ severity: response.success ? 'success' : 'error', summary: response.success ? 'Éxito' : 'Error', detail: response.message });
        if (response.success) {
          this.hideModal();
        }
      },
      error: (err) => {
        const detail = err.error?.message || 'Ocurrió un error al actualizar la adenda';
        this.messageService.add({ severity: 'error', summary: 'Error', detail });
      }
    });
  }
}
