import { Component, EventEmitter, Input, Output, inject, OnInit, OnChanges, SimpleChanges, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { CalendarStore } from '@core/store/calendar.store';
import { CreateCalendarEventRequest } from '@shared/types/calendar';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { DividerModule } from 'primeng/divider';
import { MessageModule } from 'primeng/message';
import { ModalTemplateComponent } from '@shared/components/modal-template/modal-template';

@Component({
  selector: 'app-event-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    ModalTemplateComponent,
    ButtonModule,
    InputTextModule,
    SelectModule,
    ProgressSpinnerModule,
    ToggleSwitchModule,
    DividerModule,
    MessageModule
  ],
  templateUrl: './event-modal.component.html',
})
export class EventModalComponent implements OnInit, OnChanges {
  @Input() visible = false;
  @Output() onHide = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  readonly store = inject(CalendarStore);

  eventForm!: FormGroup;
  isDayWorking = signal<boolean>(true);
  private isSaving = signal(false);

  constructor() {
    this.buildForm();

    effect(() => {
      if (this.isSaving()) {
        if (!this.store.loading() && !this.store.loadingEvents()) {
          if (!this.store.error() && this.store.workingDayUpdated()) {
            this.onHide.emit();
            this.resetModalState();
          }
          this.isSaving.set(false);
        }
      }
    });
  }

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible'] && this.visible) {
      if (this.store.selectedDay()) {
        this.isDayWorking.set(this.store.selectedDay()!.isWorkingDay);
      }
    }
  }

  private buildForm(): void {
    this.eventForm = this.fb.group({
      eventTypePublicId: ['', [Validators.required]],
      description: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9\sñÑáéíóúÁÉÍÓÚüÜ]*$/)]],
    });
  }

  private resetFormAndValidation(): void {
    this.eventForm.reset();
    this.eventForm.markAsUntouched();
    this.eventForm.markAsPristine();
    this.eventForm.get('eventTypePublicId')?.setValue('');
  }

  hideModal(): void {
    this.onHide.emit();
    this.resetModalState();
  }

  private resetModalState(): void {
    this.resetFormAndValidation();
    this.store.clearSelectedDayAndEvents();
    this.store.clearError();
    this.store.resetWorkingDayUpdated();
    this.isSaving.set(false);
  }

  onSubmit(): void {
    const selectedDay = this.store.selectedDay();
    if (!selectedDay) return;

    let operationInitiated = false;

    if (this.eventForm.valid) {
      operationInitiated = true;
      const request: CreateCalendarEventRequest = {
        ...this.eventForm.value,
        date: selectedDay.date,
      };
      this.store.createEvent(request);
      this.eventForm.patchValue({ eventTypePublicId: '', description: '' });
      this.eventForm.get('eventTypePublicId')?.markAsUntouched();
      this.eventForm.get('eventTypePublicId')?.markAsPristine();
      this.eventForm.get('description')?.markAsUntouched();
      this.eventForm.get('description')?.markAsPristine();

    } else {
      this.eventForm.markAllAsTouched();
    }

    if (this.isDayWorking() !== selectedDay.isWorkingDay) {
      operationInitiated = true;
      this.store.updateDayWorkingStatus({
        date: selectedDay.date,
        isWorkingDay: this.isDayWorking()
      });
    }

    if (operationInitiated) {
      this.isSaving.set(true);
    }
  }

  deleteEvent(eventPublicId: string): void {
    this.isSaving.set(true);
    this.store.deleteEvent(eventPublicId);
  }

  onWorkingDayToggle(newValue: boolean): void {
    const selectedDay = this.store.selectedDay();
    if (selectedDay) {
      this.isDayWorking.set(newValue);
      this.isSaving.set(true);
      this.store.updateDayWorkingStatus({
        date: selectedDay.date,
        isWorkingDay: newValue
      });
    }
  }

  get anyLoading(): boolean {
    return this.store.loading() || this.store.loadingEvents() || this.isSaving();
  }
}
