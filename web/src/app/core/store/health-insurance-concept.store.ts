import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of, finalize } from 'rxjs';
import { ConceptService } from '../services/concept.service';
import { MessageService } from 'primeng/api';
import { ConceptSelectOptionDTO } from '@shared/types/concept';
import { ApiResult } from '@shared/types/api';

interface HealthInsuranceConceptState {
  concepts: ConceptSelectOptionDTO[];
  loading: boolean;
  error: string | null;
}

const initialState: HealthInsuranceConceptState = {
  concepts: [],
  loading: false,
  error: null
};

export const HealthInsuranceConceptStore = signalStore(
  { providedIn: 'root' },
  withState<HealthInsuranceConceptState>(initialState),

  withProps(() => ({
    _conceptService: inject(ConceptService),
    _messageService: inject(MessageService)
  })),

  withComputed((store) => ({
    isEmpty: computed(() => !store.loading() && store.concepts().length === 0)
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
      patchState(store, { loading: false, error: messages.join('\n') });
    };

    const loadHealthInsuranceConcepts = rxMethod<void>(
      pipe(
        switchMap(() => {
          if (store.concepts().length > 0) {
            return of(null);
          }

          patchState(store, { loading: true, error: null });

          return store._conceptService.getSelectOptionsByCategory('EMPLOYER_CONTRIBUTION').pipe(
            tap((response: ApiResult<ConceptSelectOptionDTO[]>) => {
              if (response.success && response.data) {
                patchState(store, { 
                  concepts: [...response.data],
                  loading: false
                });
              } else {
                handleHttpError(null, 'Error al cargar conceptos de seguro de salud', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar conceptos de seguro de salud');
              return of(null);
            }),
            finalize(() => {
              patchState(store, { loading: false });
            })
          );
        })
      )
    );

    return {
      load: loadHealthInsuranceConcepts,

      refresh: () => {
        patchState(store, { concepts: [] });
        loadHealthInsuranceConcepts();
      },

      clearError: () => {
        patchState(store, { error: null });
      }
    };
  })
);

