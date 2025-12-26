import { Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AddendumTemplateService } from '@core/services/addendum-template.service';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { AddendumTypeService } from '@core/services/addendum-type.service';
import { SelectOption, StateSelectOptionDTO } from '@core/models/api.model';
import { EditorComponent } from '@shared/components/editor/editor';
import { VariableService } from '@core/services/variable.service';
import { VariableSelectOption } from '@shared/types/variable';
import { TextareaModule } from 'primeng/textarea';

@Component({
  selector: 'app-addendum-template-create-modal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    InputTextModule,
    SelectModule,
    EditorComponent,
    TextareaModule
  ],
  templateUrl: './create-modal.html',
  styleUrls: ['./create-modal.css']
})
export class AddendumTemplateCreateModal implements OnInit {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private addendumTemplateService = inject(AddendumTemplateService);
  private addendumTypeService = inject(AddendumTypeService);
  private variableService = inject(VariableService);
  private messageService = inject(MessageService);

  addendumTypes = signal<SelectOption[]>([]);
  states = signal<StateSelectOptionDTO[]>([]);
  variables = signal<VariableSelectOption[]>([]);

  templateForm = this.fb.group({
    name: ['', Validators.required],
    addendumTypePublicId: ['', Validators.required],
    statePublicId: ['', Validators.required],
    templateContent: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadAddendumTypes();
    this.loadStates();
    this.loadVariables();
  }

  loadAddendumTypes() {
    this.addendumTypeService.getAddendumTypeSelectOptions().subscribe(response => {
      if (response.success) {
        this.addendumTypes.set(response.data);
      }
    });
  }

  loadStates() {
    this.addendumTemplateService.getStatesSelectOptions().subscribe(response => {
      if (response.success) {
        this.states.set(response.data);
        const defaultState = response.data.find(s => s.default);
        if (defaultState) {
          this.templateForm.get('statePublicId')?.setValue(defaultState.publicId);
        }
      }
    });
  }

  loadVariables() {
    this.variableService.getSelectOptions().subscribe((response: any) => {
      if (response.success) {
        this.variables.set(response.data);
      }
    });
  }

  hideModal() {
    this.onHide.emit();
    this.templateForm.reset();
  }

  onContentChange(content: string) {
    this.templateForm.get('templateContent')?.setValue(content);
  }

  createTemplate() {
    if (this.templateForm.valid) {
      const formValue = this.templateForm.value;
      this.addendumTemplateService.create({
        name: formValue.name!,
        addendumTypePublicId: formValue.addendumTypePublicId!,
        statePublicId: formValue.statePublicId!,
        templateContent: formValue.templateContent!
      }).subscribe({
        next: (response) => {
          this.messageService.add({ severity: response.success ? 'success' : 'error', summary: response.success ? 'Éxito' : 'Error', detail: response.message });
          if (response.success) {
            this.hideModal();
          }
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.message || 'Ocurrió un error al crear la plantilla.' });
        }
      });
    }
  }
}
