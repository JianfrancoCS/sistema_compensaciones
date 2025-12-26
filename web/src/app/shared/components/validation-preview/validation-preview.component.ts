import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { FloatLabelModule } from 'primeng/floatlabel';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-validation-preview',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    InputTextModule,
    FloatLabelModule,
    TagModule,
    TooltipModule
  ],
  templateUrl: './validation-preview.component.html',
  styleUrl: './validation-preview.component.css'
})
export class ValidationPreviewComponent implements OnInit, OnChanges {
  @Input() regex: string = '';
  @Input() errorMessage: string = '';
  @Input() variableName: string = 'Variable';

  testValue: string = '';
  isValid: boolean = false;
  validationError: string = '';
  regexObj: RegExp | null = null;

  ngOnInit() {
    this.updateRegex();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['regex'] || changes['errorMessage']) {
      this.updateRegex();
      this.validateTestValue();
    }
  }

  private updateRegex() {
    try {
      if (this.regex) {
        this.regexObj = new RegExp(this.regex);
      } else {
        this.regexObj = null;
      }
    } catch (error) {
      this.regexObj = null;
      console.warn('Invalid regex pattern:', this.regex);
    }
  }

  onTestValueChange() {
    this.validateTestValue();
  }

  private validateTestValue() {
    if (!this.testValue.trim()) {
      this.isValid = false;
      this.validationError = '';
      return;
    }

    if (!this.regexObj) {
      this.isValid = false;
      this.validationError = 'No hay patrón de validación configurado';
      return;
    }

    try {
      this.isValid = this.regexObj.test(this.testValue);
      this.validationError = this.isValid ? '' : this.errorMessage;
    } catch (error) {
      this.isValid = false;
      this.validationError = 'Error en la validación del patrón';
    }
  }

  clearTestValue() {
    this.testValue = '';
    this.isValid = false;
    this.validationError = '';
  }

  getValidationStatusIcon(): string {
    if (!this.testValue.trim()) return 'pi-info-circle';
    return this.isValid ? 'pi-check-circle' : 'pi-times-circle';
  }

  getValidationStatusClass(): string {
    if (!this.testValue.trim()) return 'text-gray-500';
    return this.isValid ? 'text-green-500' : 'text-red-500';
  }

  getValidationStatusText(): string {
    if (!this.testValue.trim()) return 'Ingrese un valor para probar';
    return this.isValid ? 'Válido' : 'Inválido';
  }

  hasValidRegex(): boolean {
    return this.regexObj !== null && this.regex.length > 0;
  }

  copyRegexToClipboard() {
    if (this.regex) {
      navigator.clipboard.writeText(this.regex).then(() => {
        console.log('Regex copiado al portapapeles');
      }).catch(err => {
        console.error('Error al copiar regex:', err);
      });
    }
  }
}