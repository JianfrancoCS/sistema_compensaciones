import { inject } from '@angular/core';
import { signalStore, withState, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { MessageService } from 'primeng/api';
import { TareoService } from '../services/tareo.service';
import {
  TareoListDTO,
  TareoDailyDTO,
  TareoDetailDTO,
  TareoPageableRequest,
  TareoDailyPageableRequest
} from '@shared/types/tareo';

interface TareoState {
  tareos: TareoListDTO[];
  totalItems: number;
  page: number;
  size: number;
  isLoading: boolean;
  error: string | null;

  laborFilter: string | null;
  subsidiaryFilter: string | null;
  createdByFilter: string | null;
  dateFromFilter: string | null;
  dateToFilter: string | null;
  isProcessedFilter: boolean | null;
  
  sortBy: string;
  sortDirection: 'ASC' | 'DESC';

  dailyTareos: TareoDailyDTO[];
  dailyTotalItems: number;
  dailyPage: number;
  dailySize: number;
  isLoadingDaily: boolean;
  isCalculatedFilter: boolean | null;
}

const initialState: TareoState = {
  tareos: [],
  totalItems: 0,
  page: 0,
  size: 25,
  isLoading: false,
  error: null,
  laborFilter: null,
  subsidiaryFilter: null,
  createdByFilter: null,
  dateFromFilter: null,
  dateToFilter: null,
  isProcessedFilter: null,
  sortBy: 'createdAt',
  sortDirection: 'DESC',
  dailyTareos: [],
  dailyTotalItems: 0,
  dailyPage: 0,
  dailySize: 25,
  isLoadingDaily: false,
  isCalculatedFilter: null,
};

export const TareoStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withMethods((store, tareoService = inject(TareoService), messageService = inject(MessageService)) => {
    const showMessage = (message: string, severity: 'success' | 'error') => {
      messageService.add({
        severity,
        summary: severity === 'success' ? 'Éxito' : 'Error',
        detail: message
      });
    };

    const handleHttpError = (err: any, defaultMessage: string, apiResponseMessage?: string) => {
      console.error('handleHttpError triggered:', err);
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
      patchState(store, { isLoading: false, error: errorForState });
    };

    const loadTareos = rxMethod<void>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap(() => {
          const request: TareoPageableRequest = {
            page: store.page(),
            size: store.size(),
            sortBy: store.sortBy(),
            sortDirection: store.sortDirection(),
            laborPublicId: store.laborFilter() || undefined,
            subsidiaryPublicId: store.subsidiaryFilter() || undefined,
            createdBy: store.createdByFilter() || undefined,
            dateFrom: store.dateFromFilter() || undefined,
            dateTo: store.dateToFilter() || undefined,
            isProcessed: store.isProcessedFilter() !== null ? store.isProcessedFilter()! : undefined,
          };

          return tareoService.list(request).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, {
                  tareos: result.data.data,
                  totalItems: result.data.totalElements,
                  isLoading: false,
                });
              } else {
                patchState(store, { isLoading: false, tareos: [] });
                handleHttpError(null, 'Error al cargar los tareos.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar los tareos.');
              return of(null);
            })
          );
        })
      )
    );

    const deleteTareo = rxMethod<string>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((publicId) =>
          tareoService.delete(publicId).pipe(
            tap((result) => {
              if (result.success) {
                patchState(store, { isLoading: false });
                showMessage('Tareo eliminado exitosamente.', 'success');
                loadTareos();
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al eliminar el tareo.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al eliminar el tareo.');
              return of(null);
            })
          )
        )
      )
    );

    const loadDailyTareos = rxMethod<void>(
      pipe(
        tap(() => {
          patchState(store, { isLoadingDaily: true, error: null });
        }),
        switchMap(() => {
          const request: TareoDailyPageableRequest = {
            page: store.dailyPage(),
            size: store.dailySize(),
            sortBy: 'tareoDate',
            sortDirection: 'DESC',
            laborPublicId: store.laborFilter() || undefined,
            subsidiaryPublicId: store.subsidiaryFilter() || undefined,
            dateFrom: store.dateFromFilter() || undefined,
            dateTo: store.dateToFilter() || undefined,
            isCalculated: store.isCalculatedFilter() !== null ? store.isCalculatedFilter()! : undefined,
          };

          return tareoService.listDaily(request).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, {
                  dailyTareos: result.data.data,
                  dailyTotalItems: result.data.totalElements,
                  isLoadingDaily: false,
                });
              } else {
                patchState(store, { isLoadingDaily: false, dailyTareos: [] });
                handleHttpError(null, 'Error al cargar los tareos día a día.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar los tareos día a día.');
              return of(null);
            })
          );
        })
      )
    );

    return {
      loadTareos,
      deleteTareo,
      setPage: (page: number) => {
        patchState(store, { page });
        loadTareos();
      },
      setLaborFilter: (laborPublicId: string | null) => {
        patchState(store, { laborFilter: laborPublicId, page: 0 });
        loadTareos();
      },
      setSubsidiaryFilter: (subsidiaryPublicId: string | null) => {
        patchState(store, { subsidiaryFilter: subsidiaryPublicId, page: 0 });
        loadTareos();
      },
      setCreatedByFilter: (createdBy: string | null) => {
        patchState(store, { createdByFilter: createdBy, page: 0 });
        loadTareos();
      },
      setDateFromFilter: (dateFrom: string | null) => {
        patchState(store, { dateFromFilter: dateFrom, page: 0 });
        loadTareos();
      },
      setDateToFilter: (dateTo: string | null) => {
        patchState(store, { dateToFilter: dateTo, page: 0 });
        loadTareos();
      },
      setIsProcessedFilter: (isProcessed: boolean | null) => {
        patchState(store, { isProcessedFilter: isProcessed, page: 0 });
        loadTareos();
      },
      setSort: (sortBy: string, sortDirection: 'ASC' | 'DESC') => {
        patchState(store, { sortBy, sortDirection, page: 0 });
        loadTareos();
      },
      clearFilters: () => {
        patchState(store, {
          laborFilter: null,
          subsidiaryFilter: null,
          createdByFilter: null,
          dateFromFilter: null,
          dateToFilter: null,
          isProcessedFilter: null,
          sortBy: 'createdAt',
          sortDirection: 'DESC',
          page: 0
        });
        loadTareos();
      },
      clearError: () => patchState(store, { error: null }),
      init: () => {
        loadTareos();
      },
      loadDailyTareos,
      setDailyPage: (page: number) => {
        patchState(store, { dailyPage: page });
        loadDailyTareos();
      },
      setDailyLaborFilter: (laborPublicId: string | null) => {
        patchState(store, { laborFilter: laborPublicId, dailyPage: 0 });
        loadDailyTareos();
      },
      setDailySubsidiaryFilter: (subsidiaryPublicId: string | null) => {
        patchState(store, { subsidiaryFilter: subsidiaryPublicId, dailyPage: 0 });
        loadDailyTareos();
      },
      setDailyDateFromFilter: (dateFrom: string | null) => {
        patchState(store, { dateFromFilter: dateFrom, dailyPage: 0 });
        loadDailyTareos();
      },
      setDailyDateToFilter: (dateTo: string | null) => {
        patchState(store, { dateToFilter: dateTo, dailyPage: 0 });
        loadDailyTareos();
      },
      setIsCalculatedFilter: (isCalculated: boolean | null) => {
        patchState(store, { isCalculatedFilter: isCalculated, dailyPage: 0 });
        loadDailyTareos();
      },
      clearDailyFilters: () => {
        patchState(store, {
          laborFilter: null,
          subsidiaryFilter: null,
          dateFromFilter: null,
          dateToFilter: null,
          isCalculatedFilter: null,
          dailyPage: 0
        });
        loadDailyTareos();
      },
      initDaily: () => {
        loadDailyTareos();
      },
      getDetail: (publicId: string) => {
        return tareoService.getDetail(publicId);
      },
    };
  })
);

