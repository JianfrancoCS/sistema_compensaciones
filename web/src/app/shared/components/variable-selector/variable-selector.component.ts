import { Component, Input, Output, EventEmitter, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { VariableStore } from '@core/store/variables.store';
import { VariableSelectOption } from '@shared/types/variable';

@Component({
  selector: 'app-variable-selector',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    IconFieldModule,
    InputIconModule,
    InputTextModule,
    CheckboxModule
  ],
  templateUrl: './variable-selector.component.html',
  styleUrl: './variable-selector.component.css'
})
export class VariableSelectorComponent implements OnInit {
  @Input() selectedVariables: VariableSelectOption[] = [];
  @Input() allowMultiple: boolean = true;
  @Input() placeholder: string = 'Buscar variables...';
  @Output() variablesChange = new EventEmitter<VariableSelectOption[]>();
  @Output() selectionChange = new EventEmitter<VariableSelectOption[]>();

  private readonly variableStore = inject(VariableStore);

  searchText: string = '';
  private searchSubject = new Subject<string>();

  availableVariables = this.variableStore.selectOptions;
  loading = this.variableStore.searchLoading;

  ngOnInit() {
    this.setupSearch();
    this.loadInitialVariables();
  }

  private setupSearch() {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(searchText => {
      this.searchVariables(searchText);
    });
  }

  private loadInitialVariables() {
    this.variableStore.searchSelectOptions();
  }

  private searchVariables(searchText: string) {
    const params = searchText ? { name: searchText } : undefined;
    this.variableStore.searchSelectOptions(params);
  }

  onSearchChange(value: string) {
    this.searchText = value;
    this.searchSubject.next(value);
  }

  onVariableToggle(variable: VariableSelectOption, checked: boolean) {
    let updatedSelection: VariableSelectOption[];

    if (this.allowMultiple) {
      if (checked) {
        updatedSelection = [...this.selectedVariables, variable];
      } else {
        updatedSelection = this.selectedVariables.filter(v => v.publicId !== variable.publicId);
      }
    } else {
      updatedSelection = checked ? [variable] : [];
    }

    this.selectedVariables = updatedSelection;
    this.variablesChange.emit(updatedSelection);
    this.selectionChange.emit(updatedSelection);
  }

  isVariableSelected(variable: VariableSelectOption): boolean {
    return this.selectedVariables.some(v => v.publicId === variable.publicId);
  }

  clearSearch() {
    this.searchText = '';
    this.loadInitialVariables();
  }

  selectAll() {
    if (!this.allowMultiple) return;

    this.selectedVariables = [...this.availableVariables()];
    this.variablesChange.emit(this.selectedVariables);
    this.selectionChange.emit(this.selectedVariables);
  }

  clearSelection() {
    this.selectedVariables = [];
    this.variablesChange.emit(this.selectedVariables);
    this.selectionChange.emit(this.selectedVariables);
  }

  trackByVariableId(index: number, variable: VariableSelectOption): string {
    return variable.publicId;
  }
}