import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResult } from '@shared/types/api';
import {
  AvailablePeriodDTO,
  CalendarDayListDTO,
  CommandCalendarEventResponse,
  CreateCalendarEventRequest,
  EventTypeSelectOptionDTO,
  UpdateWorkingDayRequest
} from '@shared/types/calendar';

@Injectable({
  providedIn: 'root'
})
export class CalendarService {
  private http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:10000/v1/calendar';

  getAvailablePeriods(): Observable<ApiResult<AvailablePeriodDTO[]>> {
    return this.http.get<ApiResult<AvailablePeriodDTO[]>>(`${this.apiUrl}/available-periods`);
  }

  getCalendarDays(year: number, month: number): Observable<ApiResult<CalendarDayListDTO[]>> {
    const params = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    return this.http.get<ApiResult<CalendarDayListDTO[]>>(`${this.apiUrl}/days`, { params });
  }

  getEventTypes(): Observable<ApiResult<EventTypeSelectOptionDTO[]>> {
    return this.http.get<ApiResult<EventTypeSelectOptionDTO[]>>(`${this.apiUrl}/event-types`);
  }

  getEventsForDay(dayPublicId: string): Observable<ApiResult<CommandCalendarEventResponse[]>> {
    return this.http.get<ApiResult<CommandCalendarEventResponse[]>>(`${this.apiUrl}/days/${dayPublicId}/events`);
  }

  createEvent(request: CreateCalendarEventRequest): Observable<ApiResult<CommandCalendarEventResponse>> {
    return this.http.post<ApiResult<CommandCalendarEventResponse>>(`${this.apiUrl}/events`, request);
  }

  deleteEvent(eventPublicId: string): Observable<ApiResult<void>> {
    return this.http.delete<ApiResult<void>>(`${this.apiUrl}/events/${eventPublicId}`);
  }

  updateDayWorkingStatus(request: UpdateWorkingDayRequest): Observable<ApiResult<void>> {
    return this.http.put<ApiResult<void>>(`${this.apiUrl}/days/working-day`, request);
  }
}
