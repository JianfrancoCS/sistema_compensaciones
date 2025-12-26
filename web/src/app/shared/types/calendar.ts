import { PagedResult } from "./api";

export interface MonthInfoDTO {
  month: number;
  monthName: string;
}

export interface AvailablePeriodDTO {
  year: number;
  months: MonthInfoDTO[];
}

export interface CalendarDayListDTO {
  publicId: string;
  date: string; // Format: "YYYY-MM-DD"
  isWorkingDay: boolean;
  eventCount: number;
}

export interface EventTypeSelectOptionDTO {
  publicId: string;
  name: string;
}

export interface CommandCalendarEventResponse {
  publicId: string;
  description: string;
  eventTypePublicId: string;
  eventTypeName: string;
}

export interface CreateCalendarEventRequest {
  date: string; // "YYYY-MM-DD"
  eventTypePublicId: string;
  description: string;
}

export interface UpdateWorkingDayRequest {
  date: string;
  isWorkingDay: boolean;
}

export interface CalendarState {
  availablePeriods: AvailablePeriodDTO[];
  days: CalendarDayListDTO[];
  selectedPeriod: { year: number; month: number } | null;
  selectedDay: CalendarDayListDTO | null;
  dayEvents: CommandCalendarEventResponse[];
  eventTypeOptions: EventTypeSelectOptionDTO[];
  loading: boolean;
  loadingEvents: boolean; // Separate loading for modal events
  error: string | null;
  workingDayUpdated: boolean; // Nueva propiedad para el estado del día laborable
}
