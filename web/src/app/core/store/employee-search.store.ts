import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { EmployeeService } from '../services/employee.service';

interface EmployeeSearchState {
  foundEmployee: { documentNumber: string; fullName: string; position: string; subsidiaryId: string; subsidiaryName: string } | null;
  searching: boolean;
  error: string | null;
}

const initialState: EmployeeSearchState = {
  foundEmployee: null,
  searching: false,
  error: null
};

export const EmployeeSearchStore = signalStore(
  { providedIn: 'root' },
  withState<EmployeeSearchState>(initialState),

  withProps(() => ({
    _employeeService: inject(EmployeeService)
  })),

  withComputed((state) => ({
    hasFoundEmployee: computed(() => state.foundEmployee() !== null),
    hasError: computed(() => state.error() !== null)
  })),

  withMethods((store) => {
    const searchByDocumentNumber = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { searching: true, error: null, foundEmployee: null })),
        switchMap((documentNumber) => {
          return store._employeeService.searchByDocumentNumber(documentNumber).pipe(
            tap((response) => {
              if (response.success && response.data) {
                patchState(store, {
                  foundEmployee: response.data,
                  searching: false,
                  error: null
                });
              } else {
                const errorMessage = response.message || 'No se encontró empleado con el documento ingresado.';
                patchState(store, {
                  foundEmployee: null,
                  searching: false,
                  error: errorMessage
                });
              }
            }),
            catchError((err) => {
              let errorMessage = 'No se encontró empleado con el documento ingresado.';
              
              if (err?.error?.message) {
                if (Array.isArray(err.error.message)) {
                  errorMessage = err.error.message[0];
                } else if (typeof err.error.message === 'string') {
                  errorMessage = err.error.message;
                }
              } else if (err?.error?.detail) {
                errorMessage = err.error.detail;
              } else if (err?.status === 404) {
                errorMessage = 'No se encontró empleado con el documento ingresado.';
              }
              
              patchState(store, {
                foundEmployee: null,
                searching: false,
                error: errorMessage
              });
              
              return of(null);
            })
          );
        })
      )
    );

    const clearSearch = () => {
      patchState(store, { foundEmployee: null, searching: false, error: null });
    };

    return {
      foundEmployee: store.foundEmployee,
      searching: store.searching,
      error: store.error,
      hasFoundEmployee: store.hasFoundEmployee,
      hasError: store.hasError,
      searchByDocumentNumber,
      clearSearch
    };
  })
);

