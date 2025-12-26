import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { PersonService } from '../services/person.service';
import { PersonDetailsDTO } from '@shared/types/person';
export type { PersonDetailsDTO };

interface PersonSearchState {
  searchingDocument: string | null;
  foundPerson: PersonDetailsDTO | null;
  loading: boolean;
  error: string | null;
  searchStatus: 'idle' | 'searching' | 'found' | 'not_found';
}

const initialState: PersonSearchState = {
  searchingDocument: null,
  foundPerson: null,
  loading: false,
  error: null,
  searchStatus: 'idle'
};

export const PersonStore = signalStore(
  withState<PersonSearchState>(initialState),

  withProps(() => ({
    _personService: inject(PersonService)
  })),

  withComputed((state) => ({
    isSearching: computed(() => state.loading()),
    personFound: computed(() => state.searchStatus() === 'found'),
    personNotFound: computed(() => state.searchStatus() === 'not_found'),
    hasSearched: computed(() => state.searchStatus() !== 'idle')
  })),

  withMethods((store) => {
    const getErrorMessage = (err: any, defaultMessage: string): string => {
      if (err?.error && typeof err.error === 'object' && 'message' in err.error) {
        return err.error.message;
      }
      return err?.message || defaultMessage;
    };

    const searchPersonByDocument = rxMethod<string>(
      pipe(
        tap((document) => patchState(store, {
          loading: true,
          error: null,
          searchingDocument: document,
          searchStatus: 'searching'
        })),
        switchMap((document) => {
          return store._personService.findPersonByDocument(document).pipe(
            tap((response) => {
              if (response.success && response.data) {
                patchState(store, {
                  foundPerson: response.data,
                  loading: false,
                  searchStatus: 'found'
                });
              } else {
                patchState(store, {
                  foundPerson: null,
                  loading: false,
                  searchStatus: 'not_found'
                });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al buscar persona');
              patchState(store, {
                loading: false,
                error,
                foundPerson: null,
                searchStatus: 'not_found'
              });
              return of(null);
            })
          );
        })
      )
    );

    return {
      searchByDocument: (document: string) => {
        if (document && document.length === 8 && /^\d{8}$/.test(document)) {
          searchPersonByDocument(document);
        } else {
          patchState(store, {
            foundPerson: null,
            loading: false,
            searchStatus: 'idle',
            searchingDocument: null,
            error: null
          });
        }
      },

      clearSearch: () => {
        patchState(store, {
          foundPerson: null,
          loading: false,
          searchStatus: 'idle',
          searchingDocument: null,
          error: null
        });
      },

      clearError: () => {
        patchState(store, { error: null });
      }
    };
  })
);