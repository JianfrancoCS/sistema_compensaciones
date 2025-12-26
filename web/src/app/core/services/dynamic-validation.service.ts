import { Injectable, inject } from '@angular/core';
import { AbstractControl, ValidatorFn } from '@angular/forms';
import { DynamicVariableService } from './dynamic-variable.service';

@Injectable({
  providedIn: 'root'
})
export class DynamicValidationService {
  private dynamicVariableService = inject(DynamicVariableService);

  createDynamicValidator(regex: string, errorMessage: string): ValidatorFn | null {
    if (!regex) {
      return null;
    }

    return (control: AbstractControl) => {
      if (!control.value) {
        return null;
      }

      try {
        const regexPattern = new RegExp(regex);
        const isValid = regexPattern.test(control.value);

        if (!isValid) {
          return {
            dynamicValidation: {
              message: errorMessage || 'El formato no es válido',
              pattern: regex,
              actualValue: control.value
            }
          };
        }

        return null;
      } catch (error) {
        console.warn('Invalid regex pattern:', regex);
        return null;
      }
    };
  }
}