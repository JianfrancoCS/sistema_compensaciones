import { inject } from '@angular/core';
import { signalStore, withState, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { SelectOption, ApiResult } from '@shared/types/api';
import { GroupService } from '../services/group.service';
import { MessageService } from 'primeng/api';

export interface GroupState {
  selectOptions: SelectOption[];
  loading: boolean;
  error: string | null;
}

const initialState: GroupState = {
  selectOptions: [],
  loading: false,
  error: null,
};

export const GroupStore = signalStore(
  withState<GroupState>(initialState),

  withProps(() => ({
    _groupService: inject(GroupService),
    _messageService: inject(MessageService)
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
      patchState(store, { loading: false, error: errorForState });
    };

    const loadSelectOptions = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          store._groupService.getSelectOptions().pipe(
            tap((response: ApiResult<SelectOption[]>) => {
              if (response.success) {
                patchState(store, { selectOptions: response.data, loading: false });
              } else {
                patchState(store, { loading: false, error: response.message });
                handleHttpError(null, 'Error al cargar opciones de selección de grupos', response.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error al cargar opciones de selección de grupos');
              return of(null);
            })
          )
        )
      )
    );

    return {
      init: () => {
        loadSelectOptions();
      },
      refresh: () => {
        loadSelectOptions();
      }
    };
  })
);
