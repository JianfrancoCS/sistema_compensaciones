import { Component, computed, inject, signal, effect } from '@angular/core';
import { AddendumTemplateService } from '@core/services/addendum-template.service';
import { AddendumTemplateListDTO } from '@shared/types/addendum';
import { handleApiResponse, handleApiError } from '@core/utils/api-response.helper';
import { TableHelper } from '@core/utils/table-helper';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { CommonModule } from '@angular/common';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { AddendumTypeService } from '@core/services/addendum-type.service';
import { SelectOption } from '@core/models/api.model';
import { SelectModule } from 'primeng/select';
import { TooltipModule } from 'primeng/tooltip';
import { CreateTemplateAddendumComponent } from './components/create-template-addendum/create-template-addendum';

@Component({
  selector: 'app-addendum-templates',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    CommonModule,
    ToastModule,
    ConfirmDialogModule,
    SelectModule,
    TooltipModule,
    CreateTemplateAddendumComponent
  ],
  templateUrl: './addendum-templates.html',
  styleUrls: ['./addendum-templates.css'],
  providers: [MessageService, ConfirmationService]
})
export class AddendumTemplates {
  public addendumTemplateService = inject(AddendumTemplateService);
  public addendumTypeService = inject(AddendumTypeService);
  private confirmationService = inject(ConfirmationService);
  private messageService = inject(MessageService);

  addendumTemplates = this.addendumTemplateService.addendumTemplates;
  totalRecords = computed(() => this.addendumTemplates().totalElements);
  addendumTypes = signal<SelectOption[]>([]);

  showCreateOrEditPage = signal(false);
  selectedAddendumTemplateId = signal<string | null>(null);

  constructor() {
    this.loadAddendumTypes();

    effect(() => {
      const error = this.addendumTemplateService.addendumTemplates().error;
      if (error) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: error });
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

  showCreatePage() {
    this.selectedAddendumTemplateId.set(null);
    this.showCreateOrEditPage.set(true);
  }

  showEditPage(template: AddendumTemplateListDTO) {
    this.selectedAddendumTemplateId.set(template.publicId);
    this.showCreateOrEditPage.set(true);
  }

  hideCreateOrEditPage(refresh: boolean) {
    this.showCreateOrEditPage.set(false);
    this.selectedAddendumTemplateId.set(null);
    if (refresh) {
      this.addendumTemplateService.loadAddendumTemplates(0, 10, 'createdAt', 'DESC');
    }
  }

  onSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    this.addendumTemplateService.search(input.value);
  }

  onFilterByType(typeId: string) {
    this.addendumTemplateService.filterByAddendumType(typeId);
  }

  loadAddendumTemplates(event: any) {
    const { page, pageSize, sortField, sortDirection } = TableHelper.processPaginationEvent(event);
    this.addendumTemplateService.loadAddendumTemplates(page, pageSize, sortField, sortDirection);
  }

  confirmDelete(template: AddendumTemplateListDTO) {
    this.confirmationService.confirm({
      message: `¿Está seguro de que desea eliminar la plantilla "${template.name}"?`,
      header: 'Confirmación de eliminación',
      icon: 'pi pi-info-circle',
      accept: () => {
        this.deleteTemplate(template.publicId);
      },
      reject: () => {
        this.messageService.add({ severity: 'info', summary: 'Cancelado', detail: 'La eliminación ha sido cancelada.' });
      }
    });
  }

  deleteTemplate(publicId: string) {
    this.addendumTemplateService.delete(publicId).subscribe({
      next: (response) => {
        handleApiResponse(response, this.messageService, 'Plantilla de adenda eliminada correctamente');
      },
      error: (err) => {
        handleApiError(err, this.messageService, 'Error al eliminar la plantilla de adenda');
      }
    });
  }
}
