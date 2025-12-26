import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, signal, inject } from '@angular/core';
import { tap, catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
import { environment } from 'environments/environment';
import { ApiResult, PagedResult, StateSelectOptionDTO } from '@shared/types';
import {
  AddendumListDTO,
  CommandAddendumResponse,
  AddendumVariableValuePayload,
  CreateAddendumRequest,
  UpdateAddendumRequest,
  ApiPagedAddendumsResponse,
  ApiCommandAddendumResponse
} from '@shared/types/addendum';

export type {
  AddendumListDTO,
  CommandAddendumResponse,
  AddendumVariableValuePayload,
  CreateAddendumRequest,
  UpdateAddendumRequest
};

@Injectable({
  providedIn: 'root'
})
export class AddendumService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/hiring/addendums`;

  private addendumsState = signal<{ addendums: AddendumListDTO[], loading: boolean, totalElements: number, error: string | null }>({ addendums: [], loading: true, totalElements: 0, error: null });
  public addendums = this.addendumsState.asReadonly();

  private currentQueryParams = signal({
    query: '',
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  });

  constructor() {
    this.fetchAddendums();
  }

  private fetchAddendums(): void {
    this.addendumsState.update(state => ({ ...state, loading: true, error: null }));

    const params = new HttpParams()
      .set('addendumNumber', this.currentQueryParams().query)
      .set('page', this.currentQueryParams().page.toString())
      .set('size', this.currentQueryParams().pageSize.toString())
      .set('sortBy', this.currentQueryParams().sortBy)
      .set('sortDirection', this.currentQueryParams().sortDirection);

    this._http.get<ApiPagedAddendumsResponse>(this._url, { params }).pipe(
      tap(response => {
        if (response.success) {
          this.addendumsState.set({
            addendums: response.data.data,
            loading: false,
            totalElements: response.data.totalElements,
            error: null
          });
        } else {
          this.addendumsState.set({
            addendums: [],
            loading: false,
            totalElements: 0,
            error: response.message || 'An unknown error occurred.'
          });
        }
      }),
      catchError(err => {
        console.error('Error fetching addendums:', err);
        this.addendumsState.set({
          addendums: [],
          loading: false,
          totalElements: 0,
          error: err.error?.message || err.message || 'Failed to load addendums.'
        });
        return of({ success: false, message: err.error?.message || err.message || 'Failed to load addendums.', data: { data: [], totalElements: 0, pageNumber: 0, totalPages: 0, isFirst: true, isLast: true, hasNext: false, hasPrevious: false }, timeStamp: new Date().toISOString() });
      })
    ).subscribe();
  }

  public search(query: string): void {
    this.currentQueryParams.update(params => ({ ...params, query, page: 0 }));
    this.fetchAddendums();
  }

  public loadAddendums(page: number, pageSize: number, sortBy: string, sortDirection: string): void {
    this.currentQueryParams.update(params => ({ ...params, page, pageSize, sortBy: sortBy || 'createdAt', sortDirection }));
    this.fetchAddendums();
  }

  public create(request: CreateAddendumRequest): Observable<ApiResult<any>> {
    return this._http.post<ApiResult<any>>(this._url, request).pipe(
      tap(response => {
        if (response.success) {
          this.fetchAddendums();
        }
      }),
      catchError(err => {
        console.error('Error creating addendum:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to create addendum.', data: {}, timeStamp: new Date().toISOString() });
      })
    );
  }

  public update(publicId: string, request: UpdateAddendumRequest): Observable<ApiResult<any>> {
    return this._http.put<ApiResult<any>>(`${this._url}/${publicId}`, request).pipe(
      tap(response => {
        if (response.success) {
          this.fetchAddendums();
        }
      }),
      catchError(err => {
        console.error('Error updating addendum:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to update addendum.', data: {}, timeStamp: new Date().toISOString() });
      })
    );
  }

  public delete(publicId: string): Observable<ApiResult<void>> {
    return this._http.delete<ApiResult<void>>(`${this._url}/${publicId}`).pipe(
      tap(response => {
        if (response.success) {
          this.fetchAddendums();
        }
      }),
      catchError(err => {
        console.error('Error deleting addendum:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to delete addendum.', data: undefined, timeStamp: new Date().toISOString() });
      })
    );
  }

  public getAddendumForCommand(publicId: string): Observable<ApiCommandAddendumResponse> {
    return this._http.get<ApiCommandAddendumResponse>(`${this._url}/${publicId}/command`).pipe(
      catchError(err => {
        console.error('Error fetching addendum for command:', err);
        const emptyCommandResponse: CommandAddendumResponse = {
          id: '',
          addendumNumber: '',
          contractPublicId: '',
          addendumTypePublicId: '',
          statePublicId: '',
          templatePublicId: '',
          content: '',
          variables: []
        };
        return of({ success: false, message: err.error?.message || err.message || 'Failed to fetch addendum for command.', data: emptyCommandResponse, timeStamp: new Date().toISOString() });
      })
    );
  }

  public getStatesSelectOptions(): Observable<ApiResult<StateSelectOptionDTO[]>> {
    return this._http.get<ApiResult<StateSelectOptionDTO[]>>(`${this._url}/states/select-options`).pipe(
      catchError(err => {
        console.error('Error fetching states select options:', err);
        return of({ success: false, message: err.error?.message || err.message || 'Failed to fetch states select options.', data: [], timeStamp: new Date().toISOString() });
      })
    );
  }
}
