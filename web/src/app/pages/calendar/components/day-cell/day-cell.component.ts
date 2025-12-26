import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CalendarGridDay } from '../../calendar'; // Import the new type

@Component({
  selector: 'app-day-cell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './day-cell.component.html',
})
export class DayCellComponent {
  @Input() day: CalendarGridDay | null = null;
  @Output() dayClick = new EventEmitter<CalendarGridDay>();

  get dayNumber(): string {
    if (!this.day) return '';
    return this.day.date.split('-')[2].replace(/^0+/, '');
  }

  onDayClick(): void {
    if (this.day && !this.day.isOutsideMonth) {
      this.dayClick.emit(this.day);
    }
  }
}
