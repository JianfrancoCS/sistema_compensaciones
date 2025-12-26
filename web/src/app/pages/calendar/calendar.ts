import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CalendarStore } from '@core/store/calendar.store';
import { DayCellComponent } from './components/day-cell/day-cell.component';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { CalendarDayListDTO } from '@shared/types/calendar';
import { EventModalComponent } from './components/event-modal/event-modal.component';

export type CalendarGridDay = CalendarDayListDTO & { isOutsideMonth?: boolean };

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DayCellComponent,
    SelectModule,
    ButtonModule,
    EventModalComponent
  ],
  templateUrl: './calendar.html',
  providers: [CalendarStore]
})
export class CalendarComponent implements OnInit {
  readonly store = inject(CalendarStore);
  readonly weekdays = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

  isEventModalVisible = signal(false);

  readonly periodOptions = computed(() => {
    return this.store.availablePeriods().flatMap(p =>
      p.months.map(m => ({
        label: `${m.monthName} ${p.year}`,
        value: { year: p.year, month: m.month }
      }))
    );
  });

  readonly calendarGrid = computed(() => {
    const days = this.store.days();
    const period = this.store.selectedPeriod();
    if (!period || days.length === 0) return [];

    const grid: CalendarGridDay[] = [];
    const { year, month } = period;

    const firstDayOfMonth = new Date(Date.UTC(year, month - 1, 1));
    const startDayOfWeek = (firstDayOfMonth.getUTCDay() + 6) % 7;

    const prevMonthLastDay = new Date(Date.UTC(year, month - 1, 0));
    const prevMonthTotalDays = prevMonthLastDay.getUTCDate();
    for (let i = startDayOfWeek; i > 0; i--) {
      const day = prevMonthTotalDays - i + 1;
      grid.push({
        publicId: `prev-${year}-${month-1}-${day}`,
        date: `${year}-${month-1}-${day}`,
        isWorkingDay: false,
        eventCount: 0,
        isOutsideMonth: true
      });
    }

    grid.push(...days);

    const daysInGrid = grid.length;
    const nextMonthDayCount = (7 - (daysInGrid % 7)) % 7; // Calculate days needed to fill the week
    for (let i = 1; i <= nextMonthDayCount; i++) {
      grid.push({
        publicId: `next-${year}-${month + 1}-${i}`,
        date: `${year}-${month + 1}-${i}`,
        isWorkingDay: false,
        eventCount: 0,
        isOutsideMonth: true
      });
    }

    return grid;
  });

  constructor() {}

  ngOnInit(): void {
    this.store.init();
  }

  onPeriodChange(period: { year: number; month: number }): void {
    if (period) {
      this.store.selectPeriod(period);
    }
  }

  onDayCellClick(day: CalendarDayListDTO): void {
    this.store.setSelectedDay(day);
    this.isEventModalVisible.set(true);
  }

  hideEventModal(): void {
    this.isEventModalVisible.set(false);
  }
}
