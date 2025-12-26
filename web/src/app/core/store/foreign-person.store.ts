import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { ForeignPersonService } from '../services/foreign-person.service';
import { DocumentTypeService } from '../services/document-type.service';
import {
  CreateForeignPersonRequest,
  UpdateForeignPersonRequest,
  ForeignPersonResponse,
  DocumentTypeSelectOptionDTO
} from '@shared/types';

export interface ForeignPersonState {
  currentPerson: ForeignPersonResponse | null;
  documentTypes: DocumentTypeSelectOptionDTO[];
  loading: boolean;
  error: string | null;
  searchPerformed: boolean;
  isEditing: boolean;
}

const initialState: ForeignPersonState = {
  currentPerson: null,
  documentTypes: [],
  loading: false,
  error: null,
  searchPerformed: false,
  isEditing: false
};

export const ForeignPersonStore = signalStore(
  withState<ForeignPersonState>(initialState),

  withProps(() => ({
    _foreignPersonService: inject(ForeignPersonService),
    _documentTypeService: inject(DocumentTypeService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && !state.currentPerson() && state.searchPerformed()),
    isNewPerson: computed(() => !state.currentPerson() && state.searchPerformed()),
    canEdit: computed(() => state.currentPerson() && !state.isEditing()),
    canSave: computed(() => state.isEditing() || (!state.currentPerson() && state.searchPerformed()))
  })),

  withMethods((store) => {
    const getErrorMessage = (err: any, defaultMessage: string): string => {
      if (err?.error?.message) {
        return err.error.message;
      }
      if (err?.message) {
        return err.message;
      }
      return defaultMessage;
    };

    const loadDocumentTypes = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          store._documentTypeService.getDocumentTypeSelectOptions().pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  documentTypes: response.data,
                  loading: false
                });
              } else {
                patchState(store, { loading: false, error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error de conexión al cargar tipos de documento');
              patchState(store, { loading: false, error });
              return of(null);
            })
          )
        )
      )
    );


    const searchPerson = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null, searchPerformed: false })),
        switchMap((documentNumber) =>
          store._foreignPersonService.findPersonByDocument(documentNumber, false).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  currentPerson: response.data,
                  loading: false,
                  searchPerformed: true,
                  isEditing: false
                });
              } else {
                patchState(store, {
                  currentPerson: null,
                  loading: false,
                  searchPerformed: true,
                  isEditing: false,
                  error: response.message
                });
              }
            }),
            catchError((err) => {
              if (err.status === 404) {
                patchState(store, {
                  currentPerson: null,
                  loading: false,
                  searchPerformed: true,
                  isEditing: false
                });
              } else {
                const error = getErrorMessage(err, 'Error de conexión al buscar persona');
                patchState(store, {
                  currentPerson: null,
                  loading: false,
                  searchPerformed: true,
                  isEditing: false,
                  error
                });
              }
              return of(null);
            })
          )
        )
      )
    );

    const createPerson = rxMethod<CreateForeignPersonRequest>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((request) =>
          store._foreignPersonService.createForeignPerson(request).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  currentPerson: response.data,
                  loading: false,
                  isEditing: false
                });
              } else {
                patchState(store, { loading: false, error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error de conexión al crear persona');
              patchState(store, { loading: false, error });
              return of(null);
            })
          )
        )
      )
    );

    const updatePerson = rxMethod<{ documentNumber: string; request: UpdateForeignPersonRequest }>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(({ documentNumber, request }) =>
          store._foreignPersonService.updateForeignPerson(documentNumber, request).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  currentPerson: response.data,
                  loading: false,
                  isEditing: false
                });
              } else {
                patchState(store, { loading: false, error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error de conexión al actualizar persona');
              patchState(store, { loading: false, error });
              return of(null);
            })
          )
        )
      )
    );

    return {
      loadDocumentTypes,
      searchPerson,
      createPerson,
      updatePerson,

      setEditing: (isEditing: boolean) => {
        patchState(store, { isEditing });
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      clearPerson: () => {
        patchState(store, {
          currentPerson: null,
          searchPerformed: false,
          isEditing: false,
          error: null
        });
      },

      init: () => {
        loadDocumentTypes();
      }
    };
  })
);