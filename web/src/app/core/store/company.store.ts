import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState, withProps } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of, Observable } from 'rxjs';
import { MessageService } from 'primeng/api';
import { CompanyService } from '../services/company.service';
import { ApiResult } from '@shared/types/api';
import {
  CompanyDTO,
  CompanyExternalInfo,
  CreateCompanyRequest,
  UpdateCompanyRequest,
  CompanyState
} from '@shared/types/company';
import { HttpErrorResponse } from '@angular/common/http';

const initialState: CompanyState = {
  company: null,
  loading: false,
  error: null,
  externalLookupLoading: false,
  externalCompanyInfo: null,
};

export const CompanyStore = signalStore(
  withState<CompanyState>(initialState),

  withProps(() => ({
    _companyService: inject(CompanyService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isCompanyLoaded: computed(() => !!state.company()),
    hasError: computed(() => !!state.error()),
  })),

  withMethods((store) => {

    const showMessage = (message: string, severity: 'success' | 'error') => {
      store._messageService.add({
        severity,
        summary: severity === 'success' ? 'Éxito' : 'Error',
        detail: message
      });
    };

    const handleHttpError = (err: any, defaultMessage: string, apiResponseMessage?: string) => {
      let messages: string[] = [];

      if (apiResponseMessage) {
        messages.push(apiResponseMessage);
      } else if (err?.error?.message) {
        if (Array.isArray(err.error.message)) {
          messages = err.error.message;
        } else {
          messages.push(err.error.message);
        }
      } else if (err?.message) {
        messages.push(err.message);
      } else {
        messages.push(defaultMessage);
      }

      messages.forEach(msg => showMessage(msg, 'error'));

      const errorForState = messages.join('\n');
      patchState(store, { loading: false, error: errorForState, externalLookupLoading: false });
    };

    const loadCompany = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          store._companyService.getCompany().pipe(
            tap((response: ApiResult<CompanyDTO>) => {
              if (response.success) {
                const newCompanyData = { ...response.data };
                patchState(store, { company: newCompanyData, loading: false });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar la empresa', response.message);
              }
            }),
            catchError((err: HttpErrorResponse) => {
              if (err.status !== 404 && err.status !== 204) {
                handleHttpError(err, 'Error de conexión al cargar la empresa');
              }
              patchState(store, { company: null, loading: false, error: null });
              return of(null);
            })
          )
        )
      )
    );

    const createCompany = rxMethod<CreateCompanyRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) =>
          store._companyService.create(request).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage('Empresa creada exitosamente.', 'success');
                loadCompany();
              } else {
                handleHttpError(null, 'Error al crear la empresa', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al crear la empresa');
              return of(null);
            })
          )
        )
      )
    );

    const updateCompany = rxMethod<UpdateCompanyRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) =>
          store._companyService.update(request).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { loading: false });
                showMessage('Empresa actualizada exitosamente.', 'success');
                loadCompany();
              } else {
                handleHttpError(null, 'Error al actualizar la empresa', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al actualizar la empresa');
              return of(null);
            })
          )
        )
      )
    );

    const createOrUpdateCompany = (request: CreateCompanyRequest | UpdateCompanyRequest) => {
      if (store.company()?.publicId) {
        updateCompany(request as UpdateCompanyRequest);
      } else {
        createCompany(request as CreateCompanyRequest);
      }
    };

    const externalLookup = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { externalLookupLoading: true, error: null, externalCompanyInfo: null })),
        switchMap((ruc) =>
          store._companyService.externalLookup(ruc).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { externalCompanyInfo: response.data, externalLookupLoading: false });
              } else {
                handleHttpError(null, 'Error al consultar RUC', response.message);
                patchState(store, { externalLookupLoading: false });
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al consultar el RUC');
              patchState(store, { externalLookupLoading: false });
              return of(null);
            })
          )
        )
      )
    );

    return {
      loadCompany,
      createOrUpdateCompany,
      externalLookup,
      init: () => {
        loadCompany();
      }
    };
  })
);
