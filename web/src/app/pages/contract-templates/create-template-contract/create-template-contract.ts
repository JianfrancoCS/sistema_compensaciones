import { Component, inject, OnInit, signal, Output, EventEmitter, Input, computed, AfterViewInit, ViewChild, OnDestroy, effect } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CreateContractTemplateRequest, UpdateContractTemplateRequest } from '@shared/types/contract-template';
import { StateSelectOptionDTO } from '@shared/types/state';
import { ContractTypeService } from '../../../core/services/contract-type.service';
import { VariableService } from '../../../core/services/variable.service';
import { VariableSelectOption } from '@shared/types/variable';
import { ApiResult, SelectOption } from '../../../core/models/api.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { EditorComponent } from '../../../shared/components/editor/editor';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { PdfExportStyleService } from '../../../core/services/PdfExportStyleService';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ContractTemplatesStore } from '../../../core/store/contract-templates.store';
import {FloatLabel} from 'primeng/floatlabel';

@Component({
  selector: 'app-create-contract',
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
    ProgressSpinnerModule,
  ],
  providers: [MessageService],
  templateUrl: './create-template-contract.html',
  styleUrl: './create-template-contract.css'
})
export class CreateContractComponent implements OnInit, AfterViewInit, OnDestroy {
  private store = inject(ContractTemplatesStore);
  private contractTypeService = inject(ContractTypeService);
  private variableService = inject(VariableService);
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);
  private sanitizer = inject(DomSanitizer);
  private pdfExportService = inject(PdfExportStyleService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  templateId = signal<string | null>(null);

  @ViewChild(EditorComponent) editorComponent!: EditorComponent;

  templateForm!: FormGroup;
  isEditMode = computed(() => !!this.templateId());

  contractTypes = signal<SelectOption[]>([]);
  states = computed(() => this.store.state().states);
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

  constructor() {
    effect(() => {
      const states = this.states();
      if (states.length > 0 && !this.isEditMode() && !this.templateForm.get('statePublicId')?.value) {
        const defaultState = states.find(s => s.default);
        if (defaultState) {
          this.templateForm.get('statePublicId')?.setValue(defaultState.publicId, { emitEvent: false });
        }
      }
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.templateId.set(id);

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
      contractTypePublicId: ['', Validators.required],
      statePublicId: ['', Validators.required],
      templateContent: ['', Validators.required]
    });
  }

  private loadDropdownData(): void {
    this.isLoading.set(true);

    this.contractTypeService.getContractTypeSelectOptions().subscribe({
      next: (res) => { if (res.success) this.contractTypes.set(res.data); },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los tipos de contrato.' })
    });

    this.variableService.getSelectOptions().subscribe({
      next: (res: ApiResult<VariableSelectOption[]>) => { if (res.success) this.variables.set(res.data); },
      error: () => this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar las variables.' }),
      complete: () => this.isLoading.set(false)
    });
  }

  private loadTemplateForEdit(): void {
    if (!this.templateId()) return;

    this.isLoading.set(true);
    this.store.getDetails(this.templateId()!).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          const templateData = res.data;

          this.templateForm.patchValue({
            name: templateData.name,
            contractTypePublicId: templateData.contractTypePublicId,
            statePublicId: templateData.statePublicId,
            templateContent: templateData.templateContent
          });

          if (this.editorComponent) {
            this.contentChangeSubject.next(this.editorComponent.getCleanContent());
          }
        } else {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: res.message || 'No se pudo cargar la plantilla para editar.' });
          this.hideModal();
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

    const formValue = this.templateForm.value;
    const cleanTemplateContent = this.editorComponent.getCleanContent();

    const usedVars = this.usedVariables().map((variable, index) => ({
      variablePublicId: variable.publicId,
      isRequired: variable.isRequired,
      displayOrder: index
    }));

    if (this.isEditMode()) {
      const request: UpdateContractTemplateRequest = {
        name: formValue.name,
        templateContent: cleanTemplateContent,
        contractTypePublicId: formValue.contractTypePublicId,
        statePublicId: formValue.statePublicId,
        variables: usedVars
      };
      this.store.update({ publicId: this.templateId()!, request });
    } else {
      const request: CreateContractTemplateRequest = {
        name: formValue.name,
        templateContent: cleanTemplateContent,
        contractTypePublicId: formValue.contractTypePublicId,
        statePublicId: formValue.statePublicId,
        variables: usedVars
      };
      this.store.create(request);
    }

    this.messageService.add({ severity: 'success', summary: 'Éxito', detail: `Plantilla ${this.isEditMode() ? 'actualizada' : 'creada'} correctamente.` });
    this.hideModal();
  }

  goBack(): void {
    this.router.navigate(['/system/contract-templates']);
  }

  hideModal(): void {
    this.goBack();
  }
}
