import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of, filter } from 'rxjs';
import { PayslipService } from '../services/payslip.service';
import { MessageService } from 'primeng/api';
import { ApiResult, PagedResult } from '@shared/types/api';
import { PayslipListDTO, PayslipPageableRequest } from '@shared/types/payslip';

interface PayslipFilters {
  periodFrom: string | null;
  periodTo: string | null;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface PayslipState {
  payslips: PayslipListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  filters: PayslipFilters;
}

const initialState: PayslipState = {
  payslips: [],
  loading: false,
  error: null,
  totalElements: 0,
  filters: {
    periodFrom: null,
    periodTo: null,
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  },
};

export const PayslipStore = signalStore(
  { providedIn: 'root' },
  withState<PayslipState>(initialState),

  withProps(() => ({
    _payslipService: inject(PayslipService),
    _messageService: inject(MessageService),
  })),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.payslips().length === 0),
  })),

  withMethods((store) => {
    const showMessage = (message: string, severity: 'success' | 'error') => {
      store._messageService.add({
        severity,
        summary: severity === 'success' ? 'Éxito' : 'Error',
        detail: message,
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

      messages.forEach((msg) => showMessage(msg, 'error'));

      const errorForState = messages.join('\n');
      patchState(store, { loading: false, error: errorForState });
    };

    const loadPayslips = rxMethod<void>(
      pipe(
        filter(() => !store.loading()),
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          const filters = store.filters();
          const request: PayslipPageableRequest = {
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection as 'ASC' | 'DESC',
            periodFrom: filters.periodFrom || undefined,
            periodTo: filters.periodTo || undefined,
          };

          return store._payslipService.list(request).pipe(
            tap((response: ApiResult<PagedResult<PayslipListDTO>>) => {
              if (response.success) {
                patchState(store, {
                  payslips: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false });
                handleHttpError(null, 'Error al cargar las boletas', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar las boletas');
              return of(null);
            })
          );
        })
      )
    );

    return {
      updateFilter: (filterName: keyof PayslipFilters, value: any) => {
        patchState(store, {
          filters: { ...store.filters(), [filterName]: value, page: 0 },
        });
        loadPayslips();
      },

      setFilters: (filters: Partial<PayslipFilters>) => {
        patchState(store, {
          filters: { ...store.filters(), ...filters, page: 0 },
        });
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            periodFrom: null,
            periodTo: null,
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC',
          },
        });
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params },
        });
        loadPayslips();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page },
        });
        loadPayslips();
      },

      refresh: () => {
        loadPayslips();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      init: () => {
        loadPayslips();
      },
    };
  })
);

