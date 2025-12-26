import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ForeignPersonStore } from '@core/store/foreign-person.store';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { FloatLabel } from 'primeng/floatlabel';
import { TooltipModule } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-foreign-persons',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    DatePickerModule,
    SelectModule,
    FloatLabel,
    TooltipModule,
    ToastModule
  ],
  templateUrl: './foreign-persons.html',
  styleUrls: ['./foreign-persons.css']
})
export class ForeignPersonsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);
  private foreignPersonStore = inject(ForeignPersonStore);

  searchForm: FormGroup;
  personForm: FormGroup;
  searchDocument = signal('');
    maxDate = new Date();

  constructor() {
    this.searchForm = this.fb.group({
      documentNumber: ['', [Validators.required, Validators.pattern(/^\d{9}$/)]]
    });

    this.personForm = this.fb.group({
      documentNumber: ['', [Validators.required, Validators.pattern(/^\d{9}$/)]],
      names: ['', Validators.required],
      paternalLastname: ['', Validators.required],
      maternalLastname: ['', Validators.required],
      dateOfBirth: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.foreignPersonStore.init();
  }

  get loading() { return this.foreignPersonStore.loading; }
  get error() { return this.foreignPersonStore.error; }
  get currentPerson() { return this.foreignPersonStore.currentPerson; }
  get documentTypes() { return this.foreignPersonStore.documentTypes; }
  get isEmpty() { return this.foreignPersonStore.isEmpty; }
  get isNewPerson() { return this.foreignPersonStore.isNewPerson; }
  get canEdit() { return this.foreignPersonStore.canEdit; }
  get canSave() { return this.foreignPersonStore.canSave; }
  get isEditing() { return this.foreignPersonStore.isEditing; }
  get searchPerformed() { return this.foreignPersonStore.searchPerformed; }

  onSearch() {
    if (this.searchForm.valid) {
      const documentNumber = this.searchForm.get('documentNumber')?.value;
      this.searchDocument.set(documentNumber);
      this.foreignPersonStore.searchPerson(documentNumber);
    }
  }

  onEdit() {
    if (this.currentPerson()) {
      this.foreignPersonStore.setEditing(true);
      this.fillFormWithCurrentPerson();
    }
  }

  onCancel() {
    this.foreignPersonStore.setEditing(false);
    if (this.currentPerson()) {
      this.fillFormWithCurrentPerson();
    } else {
      this.clearForm();
    }
  }

  onSave() {
    if (this.personForm.valid) {
      const formData = this.personForm.value;

      if (formData.dateOfBirth instanceof Date) {
        formData.dateOfBirth = formData.dateOfBirth.toISOString().split('T')[0];
      }

      if (this.isNewPerson()) {
        this.foreignPersonStore.createPerson(formData);
      } else if (this.currentPerson()) {
        const updateData = {
          names: formData.names,
          paternalLastname: formData.paternalLastname,
          maternalLastname: formData.maternalLastname,
          dateOfBirth: formData.dateOfBirth
        };
        this.foreignPersonStore.updatePerson({
          documentNumber: this.currentPerson()!.documentNumber,
          request: updateData
        });
      }
    }
  }

  onNewSearch() {
    this.foreignPersonStore.clearPerson();
    this.searchForm.reset();
    this.personForm.reset();
    this.searchDocument.set('');
  }

  private fillFormWithCurrentPerson() {
    const person = this.currentPerson();
    if (person) {
      this.personForm.patchValue({
        documentNumber: person.documentNumber,
        names: person.names,
        paternalLastname: person.paternalLastname,
        maternalLastname: person.maternalLastname,
        dateOfBirth: '' // La API no devuelve fecha de nacimiento en la búsqueda
      });
    }
  }

  private clearForm() {
    this.personForm.reset();
    this.personForm.patchValue({
      documentNumber: this.searchDocument()
    });
  }

  onFormModeChange() {
    if (this.isNewPerson()) {
      this.clearForm();
    }
  }

  clearError() {
    this.foreignPersonStore.clearError();
  }

  private showMessage(severity: string, summary: string, detail: string) {
    this.messageService.add({ severity, summary, detail });
  }
}
