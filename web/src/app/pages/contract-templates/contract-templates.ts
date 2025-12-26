import { Component, inject, signal, effect, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

import { ContractTemplatesStore } from '@core/store/contract-templates.store';
import { ContractTypeService } from '@core/services/contract-type.service';
import { PdfExportStyleService } from '@core/services/PdfExportStyleService';

import { TableModule, TableLazyLoadEvent } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService, ConfirmationService } from 'primeng/api';

import { SelectOption } from '@shared/types';
import { StateSelectOptionDTO } from '@shared/types/state';
import { ContractTemplateListDTO, CommandContractTemplateResponse } from '@shared/types/contract-template';
import { VariableSelectOption } from '@shared/types/variable';
import {InputGroup} from 'primeng/inputgroup';
import {InputGroupAddonModule} from 'primeng/inputgroupaddon';


@Component({
  selector: 'app-contract-templates',
  templateUrl: './contract-templates.html',
  styleUrls: ['./contract-templates.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    ToastModule,
    ConfirmDialogModule,
    SelectModule,
    DialogModule,
    ProgressSpinnerModule,
    TooltipModule,
    InputGroup,
    InputGroupAddonModule
  ],
  providers: [MessageService, ConfirmationService]
})
export class ContractTemplates {
  readonly store = inject(ContractTemplatesStore);
  private readonly contractTypeService = inject(ContractTypeService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly pdfExportService = inject(PdfExportStyleService);
  private readonly router = inject(Router);

  readonly contractTemplates = computed(() => this.store.state().templates?.data ?? []);
  readonly totalRecords = computed(() => this.store.state().templates?.totalElements ?? 0);
  readonly loading = computed(() => this.store.state().loading);
  readonly states = computed(() => {
    const states = this.store.state().states;
    return states?.map(state => ({
      label: state.name,
      value: state.publicId
    })) || [];
  });
  readonly selectedState = computed(() => this.store.state().selectedState);
  readonly isEmpty = computed(() => this.contractTemplates().length === 0);

  readonly contractTypes = signal<SelectOption[]>([]);
  readonly selectedContractType = signal<SelectOption | null>(null);
  readonly displayDetailsModal = signal(false);
  readonly currentTemplateName = signal<string>('');
  readonly pdfSrc = signal<SafeResourceUrl | null>(null);
  readonly rawPdfUrl = signal<string | null>(null);
  readonly loadingPdf = signal(false);

  constructor() {
    this.loadContractTypes();

    effect(() => {
      const error = this.store.state().error;
      if (error) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: error });
      }
    });
  }

  loadContractTypes(): void {
    this.contractTypeService.getContractTypeSelectOptions().subscribe({
      next: (response) => {
        if (response.success) this.contractTypes.set(response.data);
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: err.message || 'Ocurrió un error al cargar tipos de contrato.'
        });
      }
    });
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.store.search(query);
  }

  onContractTypeSelectChange(typeId: string): void {
    const selectedType = this.contractTypes().find(t => t.publicId === typeId) || null;
    this.selectedContractType.set(selectedType);
    this.store.filterByContractType(typeId || null);
  }

  onStateSelectChange(publicId: string | null): void {
    const originalStates = this.store.state().states;
    const state = originalStates.find(s => s.publicId === publicId) ?? null;
    this.store.onStateSelectChange(state);
    this.store.filterByState(publicId);
  }

  loadContractTemplates(event: TableLazyLoadEvent): void {
    const page = (event.first ?? 0) / (event.rows ?? 10);
    const size = event.rows ?? 10;
    const sortBy = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField || 'createdAt';
    const sortDirection = event.sortOrder === 1 ? 'ASC' : 'DESC';
    this.store.loadPage({ page, size, sortBy, sortDirection });
  }

  showTemplateDetails(template: ContractTemplateListDTO): void {
    this.displayDetailsModal.set(true);
    this.loadingPdf.set(true);
    this.pdfSrc.set(null);
    this.rawPdfUrl.set(null);
    this.currentTemplateName.set(template.name);

    this.store.getDetails(template.publicId).subscribe({
      next: async (response) => {
        if (response && response.data) {
          try {
            const variablesForPdf: VariableSelectOption[] = response.data.variables.map(v => ({
              publicId: v.publicId,
              code: v.code,
              name: v.name,
              defaultValue: v.defaultValue ?? '',
              isRequired: v.isRequired
            }));

            const pdfDataUrl = await this.pdfExportService.exportQuillContentHtml(
              response.data.templateContent ?? '',
              variablesForPdf,
              response.data.name
            );

            this.rawPdfUrl.set(pdfDataUrl);
            const sanitizedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(pdfDataUrl);
            this.pdfSrc.set(sanitizedUrl);
          } catch (error) {
            console.error('Error al generar el PDF:', error);
            this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Ocurrió un error al generar el PDF.' });
          } finally {
            this.loadingPdf.set(false);
          }
        } else {
          this.loadingPdf.set(false);
          this.displayDetailsModal.set(false);
        }
      },
      error: (err) => {
        this.loadingPdf.set(false);
        this.displayDetailsModal.set(false);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.message || 'Error al cargar el contenido de la plantilla.' });
      }
    });
  }

  hideDetailsModal(): void {
    this.displayDetailsModal.set(false);
    this.currentTemplateName.set('');
    if (this.rawPdfUrl()) {
      URL.revokeObjectURL(this.rawPdfUrl()!);
    }
    this.pdfSrc.set(null);
    this.rawPdfUrl.set(null);
    this.loadingPdf.set(false);
  }

  navigateToCreate(): void {
    this.router.navigate(['/system/contract-templates/create']);
  }

  navigateToEdit(template: ContractTemplateListDTO): void {
    this.router.navigate(['/system/contract-templates/edit', template.publicId]);
  }

  confirmDelete(template: ContractTemplateListDTO): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que quieres eliminar la plantilla ${template.name}?`,
      header: 'Confirmar Eliminación',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.store.delete(template.publicId);
      }
    });
  }

  downloadPdf(): void {
    if (this.rawPdfUrl()) {
      const link = document.createElement('a');
      link.href = this.rawPdfUrl()!;
      link.download = `${this.currentTemplateName() || 'contrato'}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else {
      this.messageService.add({ severity: 'warn', summary: 'Advertencia', detail: 'No hay PDF para descargar.' });
    }
  }
}
