import { Component, EventEmitter, inject, Input, Output, OnChanges, SimpleChanges, signal, effect } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConceptStore, ConceptListDTO, UpdateConceptRequest } from '@core/store/concept.store';
import { CommonModule } from '@angular/common';
import { ModalTemplateComponent } from '../../../../shared/components/modal-template/modal-template';
import { ConceptService } from '../../../../core/services/concept.service';
import { MessageModule } from 'primeng/message';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-concept-update-modal',
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
  templateUrl: './update-concept-modal.html',
  styleUrl: './update-concept-modal.css'
})
export class ConceptUpdateModal implements OnChanges {
  @Input() visible: boolean = false;
  @Input() concept!: ConceptListDTO;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  protected store = inject(ConceptStore);
  private http = inject(HttpClient);

  private isUpdating = signal(false);
  categories = signal<Array<{publicId: string, code: string, name: string}>>([]);
  conceptDetails = signal<any>(null);

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
      if (!this.isUpdating()) {
        return;
      }

      if (!this.store.loading()) {
        if (!this.store.error()) {
          this.hideModal();
        }
        this.isUpdating.set(false);
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

  async ngOnChanges(changes: SimpleChanges): Promise<void> {
    if (changes['visible'] && this.visible && this.concept) {
      try {
        const response = await firstValueFrom(this.store.getDetails(this.concept.publicId));
        if (response && response.success) {
          this.conceptDetails.set(response.data);
          this.conceptForm.patchValue({
            code: response.data.code,
            name: response.data.name,
            description: response.data.description || '',
            categoryPublicId: response.data.categoryPublicId,
            value: response.data.value !== null ? response.data.value : null,
            calculationPriority: response.data.calculationPriority
          });
        }
      } catch (error) {
        console.error('Error loading concept details:', error);
        this.conceptForm.patchValue({
          code: this.concept.code,
          name: this.concept.name,
          description: this.concept.description || '',
          value: this.concept.value !== null ? this.concept.value : null,
          calculationPriority: this.concept.calculationPriority
        });
      }
    }
  }

  hideModal() {
    this.onHide.emit();
    this.conceptForm.reset();
    this.conceptForm.patchValue({ calculationPriority: 100 });
    this.store.clearError();
  }

  updateConcept() {
    if (this.conceptForm.invalid) {
      this.conceptForm.markAllAsTouched();
      return;
    }

    const formValue = this.conceptForm.value;
    const request: UpdateConceptRequest = {
      code: formValue.code!,
      name: formValue.name!,
      description: formValue.description || undefined,
      categoryPublicId: formValue.categoryPublicId!,
      value: formValue.value !== null ? formValue.value : undefined,
      calculationPriority: formValue.calculationPriority || 100
    };

    this.isUpdating.set(true);
    this.store.update({ publicId: this.concept.publicId, request });
  }
}

