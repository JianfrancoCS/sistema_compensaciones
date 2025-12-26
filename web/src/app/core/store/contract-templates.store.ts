import { Injectable, inject } from '@angular/core';
import { patchState, signalState } from '@ngrx/signals';
import { ContractTemplatesService } from '../services/contract-templates.service';
import { PagedResult, StateSelectOptionDTO } from '@shared/types';
import { ContractTemplateListDTO, ContractTemplatePageableRequest, CreateContractTemplateRequest, UpdateContractTemplateRequest } from '@shared/types/contract-template';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

export interface ContractTemplatesState {
  templates: PagedResult<ContractTemplateListDTO> | null;
  states: StateSelectOptionDTO[];
  loading: boolean;
  error: string | null;
  selectedState: StateSelectOptionDTO | null;
  filter: {
    name: string | null;
    contractTypePublicId: string | null;
    statePublicId: string | null;
  };
  pagination: {
    page: number;
    size: number;
    sortBy: string;
    sortDirection: string;
  };
}

const initialState: ContractTemplatesState = {
  templates: null,
  states: [],
  loading: false,
  error: null,
  selectedState: null,
  filter: {
    name: null,
    contractTypePublicId: null,
    statePublicId: null,
  },
  pagination: {
    page: 0,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  },
};

@Injectable({ providedIn: 'root' })
export class ContractTemplatesStore {
  private readonly service = inject(ContractTemplatesService);
  readonly state = signalState(initialState);

  constructor() {
    this.loadStatesMethod();
    this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
  }

  readonly loadTemplates = rxMethod<{
    pagination: ContractTemplatesState['pagination'];
    filter: ContractTemplatesState['filter'];
  }>(
    pipe(
      tap(() => patchState(this.state, { loading: true, error: null })),
      switchMap(({ pagination, filter }) => {
        const request: ContractTemplatePageableRequest = { ...pagination, ...filter };
        return this.service.getContractTemplates(request).pipe(
          tap(response => {
            if (response.success) {
              patchState(this.state, { templates: response.data, loading: false });
            }
          }),
          catchError((err: HttpErrorResponse) => {
            patchState(this.state, { loading: false, error: err.message });
            return of(null);
          })
        );
      })
    )
  );

  private loadStatesMethod(): void {
    this.service.getStates().subscribe({
      next: (response) => {
        if (response.success) {
          patchState(this.state, { states: response.data });
        }
      },
      error: (err: HttpErrorResponse) => {
        patchState(this.state, { error: err.message });
      }
    });
  }

  readonly delete = rxMethod<string>(
    pipe(
      tap(() => patchState(this.state, { loading: true })),
      switchMap((publicId: string) =>
        this.service.delete(publicId).pipe(
          tap(() => {
            this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
          }),
          catchError((err: HttpErrorResponse) => {
            patchState(this.state, { loading: false, error: err.message });
            return of(null);
          })
        )
      )
    )
  );

  search(name: string | null) {
    patchState(this.state, { filter: { ...this.state.filter(), name } });
    this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
  }

  clearSearch() {
    patchState(this.state, { filter: { ...this.state.filter(), name: null } });
    this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
  }

  filterByContractType(contractTypePublicId: string | null) {
    patchState(this.state, { filter: { ...this.state.filter(), contractTypePublicId } });
    this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
  }

  filterByState(statePublicId: string | null) {
    patchState(this.state, { filter: { ...this.state.filter(), statePublicId } });
    this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
  }

  loadPage(pagination: ContractTemplatesState['pagination']) {
    patchState(this.state, { pagination });
    this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
  }

  onStateSelectChange(state: StateSelectOptionDTO | null) {
    patchState(this.state, { selectedState: state });
  }

  getDetails(publicId: string) {
    return this.service.getDetails(publicId);
  }

  getContent(publicId: string) {
    return this.service.getContent(publicId);
  }

  readonly create = rxMethod<CreateContractTemplateRequest>(
    pipe(
      tap(() => patchState(this.state, { loading: true })),
      switchMap((request) =>
        this.service.create(request).pipe(
          tap(() => {
            this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
          }),
          catchError((err: HttpErrorResponse) => {
            patchState(this.state, { loading: false, error: err.message });
            return of(null);
          })
        )
      )
    )
  );

  readonly update = rxMethod<{ publicId: string; request: UpdateContractTemplateRequest }>(
    pipe(
      tap(() => patchState(this.state, { loading: true })),
      switchMap(({ publicId, request }) =>
        this.service.update(publicId, request).pipe(
          tap(() => {
            this.loadTemplates({ pagination: this.state.pagination(), filter: this.state.filter() });
          }),
          catchError((err: HttpErrorResponse) => {
            patchState(this.state, { loading: false, error: err.message });
            return of(null);
          })
        )
      )
    )
  );
}
