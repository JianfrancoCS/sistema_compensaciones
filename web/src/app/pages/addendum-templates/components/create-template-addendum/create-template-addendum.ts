import { Component, inject, OnInit, signal, Output, EventEmitter, Input, computed, AfterViewInit, ViewChild, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { AddendumTemplateService } from '@core/services/addendum-template.service';
import { CreateAddendumTemplateRequest, UpdateAddendumTemplateRequest } from '@shared/types/addendum';
import { AddendumTypeService } from '@core/services/addendum-type.service';
import { VariableService } from '@core/services/variable.service';
import { VariableSelectOption } from '@shared/types/variable';
import { ApiResult, SelectOption, StateSelectOptionDTO } from '@core/models/api.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { EditorComponent } from '@shared/components/editor/editor';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { PdfExportStyleService } from '@core/services/PdfExportStyleService';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-create-template-addendum',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ButtonModule,
    ToastModule,
    InputTextModule,
    SelectModule,
    EditorComponent,
    InputGroupModule,
    InputGroupAddonModule,
    ProgressSpinnerModule
  ],
  providers: [MessageService],
  templateUrl: './create-template-addendum.html',
  styleUrls: ['./create-template-addendum.css']
})
export class CreateTemplateAddendumComponent implements OnInit, AfterViewInit, OnDestroy {
  private addendumTemplateService = inject(AddendumTemplateService);
  private addendumTypeService = inject(AddendumTypeService);
  private variableService = inject(VariableService);
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);
  private sanitizer = inject(DomSanitizer);
  private pdfExportService = inject(PdfExportStyleService);

  @Input() templateId: string | null = null;
  @Output() onBack = new EventEmitter<boolean>();

  @ViewChild(EditorComponent) editorComponent!: EditorComponent;

  templateForm!: FormGroup;
  isEditMode = computed(() => !!this.templateId);

  addendumTypes = signal<SelectOption[]>([]);
  states = signal<StateSelectOptionDTO[]>([]);
  variables = signal<VariableSelectOption[]>([]);
  usedVariables = signal<VariableSelectOption[]>([]);
  isLoading = signal<boolean>(false);

  variableSearchTerm = signal<string>('');
  filteredVariables = computed(() => {
    const searchTerm = this.variableSearchTerm().toLowerCase();
    if (!searchTerm) {
      return this.variables();
    }
    return this.variables().filter(variable =>
      variable.name.toLowerCase().includes(searchTerm) ||
      variable.code.toLowerCase().includes(searchTerm)
    );
  });

  pdfPreviewUrl = signal<SafeResourceUrl | null>(null);
  private contentChangeSubject = new Subject<string>();
  private contentChangeSubscription?: Subscription;

  ngOnInit(): void {
    this.initForm();
    this.loadDropdownData();
    if (this.isEditMode()) {
      this.loadTemplateForEdit();
    }
    this.setupPdfPreviewDebounce();
  }

  ngAfterViewInit(): void {
    if (this.isEditMode() && this.templateForm.get('templateContent')?.value && this.editorComponent) {
      this.contentChangeSubject.next(this.editorComponent.getCleanContent());
    }
  }

  ngOnDestroy(): void {
    this.contentChangeSubscription?.unsubscribe();
  }

  private initForm(): void {
    this.templateForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      addendumTypePublicId: ['', Validators.required],
      statePublicId: ['', Validators.required],
      templateContent: ['', Validators.required]
    });
  }

  private loadDropdownData(): void {
    this.isLoading.set(true);

    this.addendumTypeService.getAddendumTypeSelectOptions().subscribe({
      next: (res) => { if (res.success) this.addendumTypes.set(res.data); },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los tipos de adenda.' })
    });

    this.addendumTemplateService.getStatesSelectOptions().subscribe({
      next: (res) => {
        if (res.success) {
          this.states.set(res.data);
          const defaultState = res.data.find(s => s.default);
          if (defaultState && !this.isEditMode()) {
            this.templateForm.get('statePublicId')?.setValue(defaultState.publicId);
          }
        }
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los estados.' })
    });

    this.variableService.getSelectOptions().subscribe({
      next: (res: any) => { if (res.success) this.variables.set(res.data); },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar las variables.' }),
      complete: () => this.isLoading.set(false)
    });
  }

  private loadTemplateForEdit(): void {
    if (!this.templateId) return;

    this.isLoading.set(true);
    this.addendumTemplateService.getAddendumTemplateForCommand(this.templateId).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const templateData = res.data;
          this.templateForm.patchValue({
            name: templateData.name,
            addendumTypePublicId: templateData.addendumTypePublicId,
            statePublicId: templateData.statePublicId,
            templateContent: templateData.templateContent
          });
          if (this.editorComponent) {
            this.contentChangeSubject.next(this.editorComponent.getCleanContent());
          }
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: res.message || 'No se pudo cargar la plantilla para editar.' });
          this.goBack(false);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Ocurrió un error al cargar la plantilla.' });
        console.error(err);
      },
      complete: () => this.isLoading.set(false)
    });
  }

  onVariableDragStart(event: DragEvent, variable: VariableSelectOption): void {
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'copy';
      event.dataTransfer.setData('text/plain', variable.code);
    }
  }

  onEditorContentChange(content: string): void {
    this.templateForm.get('templateContent')?.setValue(content, { emitEvent: false });
    if (this.editorComponent) {
      this.contentChangeSubject.next(this.editorComponent.getCleanContent());
    }
  }

  onEditorUsedVariablesChange(usedVariables: VariableSelectOption[]): void {
    this.usedVariables.set(usedVariables);
  }

  private setupPdfPreviewDebounce(): void {
    this.contentChangeSubscription = this.contentChangeSubject.pipe(
      debounceTime(1000),
      distinctUntilChanged()
    ).subscribe(async (cleanContent) => {
      if (cleanContent) {
        try {
          const pdfBlobUrl = await this.pdfExportService.exportQuillContentHtml(
            cleanContent,
            this.variables()
          );
          this.pdfPreviewUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(pdfBlobUrl));
        } catch (error) {
          console.error('Error generando vista previa del PDF:', error);
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo generar la vista previa del PDF.' });
          this.pdfPreviewUrl.set(null);
        }
      } else {
        this.pdfPreviewUrl.set(null);
      }
    });
  }

  saveTemplate(): void {
    if (this.templateForm.invalid) {
      this.templateForm.markAllAsTouched();
      this.messageService.add({ severity: 'warn', summary: 'Formulario Inválido', detail: 'Por favor, complete todos los campos requeridos.' });
      return;
    }

    this.isLoading.set(true);

    const formValue = this.templateForm.value;
    const cleanTemplateContent = this.editorComponent.getCleanContent();

    const usedVars = this.usedVariables().map((variable, index) => ({
      variablePublicId: variable.publicId,
      isRequired: false, // Hardcoded to false as it's not available in the source DTO
      displayOrder: index + 1
    }));

    const request: CreateAddendumTemplateRequest | UpdateAddendumTemplateRequest = {
      name: formValue.name,
      addendumTypePublicId: formValue.addendumTypePublicId,
      statePublicId: formValue.statePublicId,
      templateContent: cleanTemplateContent,
      variables: usedVars
    };

    const operation = this.isEditMode()
      ? this.addendumTemplateService.update(this.templateId!, request as UpdateAddendumTemplateRequest)
      : this.addendumTemplateService.create(request as CreateAddendumTemplateRequest);

    operation.subscribe({
      next: (response: ApiResult<any>) => {
        this.isLoading.set(false);
        if (response.success) {
          this.messageService.add({ severity: 'success', summary: 'Éxito', detail: response.message });
          this.goBack(true);
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error al Guardar', detail: response.message });
          if (response.errors) {
            Object.entries(response.errors).forEach(([field, message]) => {
              this.messageService.add({ severity: 'error', summary: `Error en ${field}`, detail: message, life: 5000 });
            });
          }
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.messageService.add({ severity: 'error', summary: 'Error de Conexión', detail: 'No se pudo comunicar con el servidor.' });
        console.error(err);
      }
    });
  }

  goBack(refresh: boolean = false): void {
    this.onBack.emit(refresh);
  }
}
