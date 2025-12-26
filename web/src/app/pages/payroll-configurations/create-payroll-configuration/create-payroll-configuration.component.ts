import { Component, OnInit, inject, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PayrollConfigurationStore } from '@core/store/payroll-configuration.store';
import { MultiSelectModule } from 'primeng/multiselect';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { ConceptSelectOptionDTO } from '@shared/types/concept';

interface ConceptCategory {
  name: string;
  concepts: ConceptSelectOptionDTO[];
}

@Component({
  selector: 'app-create-payroll-configuration',
  standalone: true,
  imports: [CommonModule, FormsModule, MultiSelectModule, ButtonModule, CheckboxModule],
  templateUrl: './create-payroll-configuration.component.html',
})
export class CreatePayrollConfigurationComponent implements OnInit {
  readonly store = inject(PayrollConfigurationStore);
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  selectedConceptsForCreation = signal<string[]>([]);
  mode = signal<'create' | 'edit'>('create');

  private translateCategory(categoryName: string): string {
    const translations: Record<string, string> = {
      'Income': 'Ingresos',
      'Retirement': 'Jubilación',
      'Employer Contributions': 'Aportes del Empleador',
      'Deductions': 'Descuentos',
      'Bonuses': 'Bonificaciones',
      'Benefits': 'Beneficios'
    };
    return translations[categoryName] || categoryName;
  }

  groupedConceptsByCategory = computed<ConceptCategory[]>(() => {
    const concepts = this.store.availableConcepts();
    const categoriesMap = new Map<string, ConceptCategory>();

    concepts.forEach(concept => {
      const originalCategoryName = concept.categoryName || 'Sin Categoría';
      const translatedCategoryName = this.translateCategory(originalCategoryName);
      if (!categoriesMap.has(translatedCategoryName)) {
        categoriesMap.set(translatedCategoryName, { name: translatedCategoryName, concepts: [] });
      }
      categoriesMap.get(translatedCategoryName)?.concepts.push(concept);
    });

    return Array.from(categoriesMap.values());
  });

  constructor() {
    effect(() => {
      if (this.mode() === 'edit') {
        const conceptAssignments = this.store.conceptAssignments();
        if (conceptAssignments.length > 0) {
          const assignedConceptIds = conceptAssignments
            .filter(c => c.isAssigned)
            .map(c => c.conceptPublicId);
          this.selectedConceptsForCreation.set(assignedConceptIds);
        }
      }
    }, { allowSignalWrites: true });
  }

  ngOnInit(): void {
    this.store.loadAvailableConcepts();

    if (this.router.url.includes('/payroll-configurations/edit')) {
      this.mode.set('edit');
      this.store.loadConceptAssignments();
    } else {
      this.mode.set('create');
    }
  }

  onConceptSelectionChange(conceptPublicId: string, isChecked: boolean): void {
    this.selectedConceptsForCreation.update(currentSelected => {
      if (isChecked) {
        return [...currentSelected, conceptPublicId];
      } else {
        return currentSelected.filter(id => id !== conceptPublicId);
      }
    });
  }

  saveConfiguration(): void {
    if (this.mode() === 'create') {
      this.store.createPayrollConfiguration({ conceptsPublicIds: this.selectedConceptsForCreation() });
    } else if (this.mode() === 'edit') {
      this.store.updateConceptAssignments({ conceptPublicIds: this.selectedConceptsForCreation() });
    }
    this.router.navigate(['/system/payroll-configurations/detail']);
  }

  cancelCreation(): void {
    this.router.navigate(['/system/payroll-configurations/detail']);
  }
}
