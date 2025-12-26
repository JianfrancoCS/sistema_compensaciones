import { Component, effect, EventEmitter, inject, Input, Output, signal } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateConceptRequest } from '@shared/types/concept';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { ConceptStore } from '../../../../core/store/concept.store';
import { ConceptService } from '../../../../core/services/concept.service';
import { MessageModule } from 'primeng/message';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-concept-create-modal',
  standalone: true,
  imports: [
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ReactiveFormsModule,
    CommonModule,
    ModalTemplateComponent,
    MessageModule
  ],
  templateUrl: './create-concept-modal.html',
  styleUrl: './create-concept-modal.css'
})
export class ConceptCreateModal {
  @Input() visible: boolean = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(ConceptStore);
  private http = inject(HttpClient);

  private isCreating = signal(false);
  categories = signal<Array<{publicId: string, code: string, name: string}>>([]);

  conceptForm = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(20)]],
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.maxLength(255)]],
    categoryPublicId: ['', [Validators.required]],
    value: [null as number | null],
    calculationPriority: [100, [Validators.required, Validators.min(0)]]
  });

  constructor() {
    this.loadCategories();
    
    effect(() => {
      if (!this.isCreating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isCreating.set(false);
      }
    });
  }

  async loadCategories() {
    try {
      const response = await firstValueFrom(
        this.http.get<{success: boolean, data: Array<{publicId: string, code: string, name: string}>}>(`${environment.apiUrl}/v1/concepts/categories`)
      );
      if (response && response.success && response.data) {
        this.categories.set(response.data);
      }
    } catch (error) {
      console.error('Error loading categories:', error);
    }
  }

  hideModal() {
    this.onHide.emit();
    this.conceptForm.reset();
    this.conceptForm.patchValue({ calculationPriority: 100 });
    this.store.clearError();
  }

  createConcept() {
    if (this.conceptForm.valid) {
      const formValue = this.conceptForm.value;
      const request: CreateConceptRequest = {
        code: formValue.code!,
        name: formValue.name!,
        description: formValue.description || undefined,
        categoryPublicId: formValue.categoryPublicId!,
        value: formValue.value !== null ? formValue.value : undefined,
        calculationPriority: formValue.calculationPriority || 100
      };
      this.isCreating.set(true);
      this.store.create(request);
    } else {
      this.conceptForm.markAllAsTouched();
    }
  }
}

