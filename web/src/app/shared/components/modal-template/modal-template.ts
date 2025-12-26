import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal-template',
  standalone: true,
  imports: [
    DialogModule,
    ButtonModule,
    CommonModule
  ],
  templateUrl: './modal-template.html',
  styleUrl: './modal-template.css'
})
export class ModalTemplateComponent {
  @Input() visible: boolean = false;
  @Input() headerText: string = '';
  @Input() submitButtonLabel: string = 'Guardar';
  @Input() cancelButtonLabel: string = 'Cancelar';
  @Input() isSubmitDisabled: boolean = false;
  @Input() showSubmitButton: boolean = true;
  @Input() styleClass: string = '';
  @Input() width: string = '25rem';

  @Output() onHide = new EventEmitter<void>();
  @Output() onSubmit = new EventEmitter<void>();

  hideModal() {
    this.onHide.emit();
  }

  submit() {
    this.onSubmit.emit();
  }
}
