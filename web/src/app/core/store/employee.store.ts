import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { SelectOption, StateSelectOptionDTO } from '@shared/types';
import {
  EmployeeListDTO,
  CreateEmployeeRequest,
  UpdateEmployeeRequest,
  EmployeeParams,
  CommandEmployeeResponse,
  EmployeeDetailsDTO
} from '@shared/types/employee';
import { EmployeeService } from '../services/employee.service';
import { SubsidiaryService } from '../services/subsidiary.service';
import { PositionService } from '../services/position.service';
import { PersonService } from '../services/person.service';
import { LocationService } from '../services/location.service';
import { MessageService } from 'primeng/api';
import { PersonDetailsDTO } from '@shared/types/person';

interface EmployeeFilters {
  documentNumber: string;
  personName: string;
  subsidiaryPublicId: string | null;
  positionPublicId: string | null;
  isNational: boolean | null;
  page: number;
  pageSize: number;
  sortBy: string;
  sortDirection: string;
}

interface EmployeeState {
  employees: EmployeeListDTO[];
  loading: boolean;
  error: string | null;
  totalElements: number;
  subsidiarySelectOptions: SelectOption[];
  positionSelectOptions: SelectOption[];
  stateSelectOptions: StateSelectOptionDTO[];
  filters: EmployeeFilters;
  successMessage: string | null;
  foundPerson: PersonDetailsDTO | null;
  searchingPerson: boolean;
  personSearchError: string | null;
  foundEmployee: { documentNumber: string; fullName: string; position: string; subsidiaryId: string; subsidiaryName: string } | null;
  searchingEmployee: boolean;
  employeeSearchError: string | null;
}

const initialState: EmployeeState = {
  employees: [],
  loading: false,
  error: null,
  totalElements: 0,
  subsidiarySelectOptions: [],
  positionSelectOptions: [],
  stateSelectOptions: [],
  filters: {
    documentNumber: '',
    personName: '',
    subsidiaryPublicId: null,
    positionPublicId: null,
    isNational: null,
    page: 0,
    pageSize: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  },
  successMessage: null,
  foundPerson: null,
  searchingPerson: false,
  personSearchError: null,
  foundEmployee: null,
  searchingEmployee: false,
  employeeSearchError: null
};

export const EmployeeStore = signalStore(
  withState<EmployeeState>(initialState),

  withProps(() => ({
    _employeeService: inject(EmployeeService),
    _subsidiaryService: inject(SubsidiaryService),
    _positionService: inject(PositionService),
    _personService: inject(PersonService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    isEmpty: computed(() => !state.loading() && state.employees().length === 0)
  })),

  withMethods((store) => {
    const showMessage = (message: string, severity: 'success' | 'error' | 'warn' | 'info') => {
      store._messageService.add({
        severity,
        summary: severity === 'success' ? 'Éxito' : severity === 'error' ? 'Error' : severity === 'warn' ? 'Advertencia' : 'Información',
        detail: message
      });
    };

    const getErrorMessage = (err: any, defaultMessage: string): string => {
      if (err?.error && typeof err.error === 'object' && 'message' in err.error) {
        return err.error.message;
      }
      return err?.message || defaultMessage;
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

    const loadEmployees = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null, successMessage: null })),
        switchMap(() => {
          const filters = store.filters();
          const params: EmployeeParams = {
            documentNumber: filters.documentNumber,
            personName: filters.personName,
            subsidiaryPublicId: filters.subsidiaryPublicId || undefined,
            positionPublicId: filters.positionPublicId || undefined,
            isNational: filters.isNational !== null ? filters.isNational : undefined,
            page: filters.page,
            size: filters.pageSize,
            sortBy: filters.sortBy,
            sortDirection: filters.sortDirection
          };

          return store._employeeService.getEmployees(params).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, {
                  employees: response.data.data,
                  totalElements: response.data.totalElements,
                  loading: false,
                });
              } else {
                patchState(store, { loading: false, error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error de conexión al cargar empleados');
              patchState(store, { loading: false, error });
              return of(null);
            })
          );
        })
      )
    );

    const loadSubsidiarySelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._subsidiaryService.getSelectOptions().pipe(
            tap((response) => {
              patchState(store, { subsidiarySelectOptions: response.success ? response.data : [] });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar opciones de selección de filiales');
              patchState(store, { error });
              return of(null);
            })
          )
        )
      )
    );

    const loadPositionSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._positionService.getPositionsSelectOptions().pipe(
            tap((response) => {
              patchState(store, { positionSelectOptions: response.success ? response.data : [] });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar opciones de selección de cargos');
              patchState(store, { error });
              return of(null);
            })
          )
        )
      )
    );

    const loadStateSelectOptions = rxMethod<void>(
      pipe(
        switchMap(() =>
          store._employeeService.getStatesForSelect().pipe(
            tap((response) => {
              patchState(store, { stateSelectOptions: response.success ? response.data : [] });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar opciones de selección de estados');
              patchState(store, { error });
              return of(null);
            })
          )
        )
      )
    );

    return {
      updateFilter: (filters: { documentNumber?: string, personName?: string, subsidiaryPublicId?: string | null, positionPublicId?: string | null, isNational?: boolean | null }) => {
        patchState(store, {
          filters: { ...store.filters(), ...filters, page: 0 }
        });
        loadEmployees();
      },

      filterByNationality: (isNational: boolean | null) => {
        patchState(store, {
          filters: { ...store.filters(), isNational, page: 0 }
        });
        loadEmployees();
      },

      search: (query: string) => {
        patchState(store, {
          filters: { ...store.filters(), documentNumber: query, page: 0 }
        });
        loadEmployees();
      },

      clearSearch: () => {
        patchState(store, {
          filters: { ...store.filters(), documentNumber: '', page: 0 }
        });
        loadEmployees();
      },

      resetFilters: () => {
        patchState(store, {
          filters: {
            documentNumber: '',
            personName: '',
            subsidiaryPublicId: null,
            positionPublicId: null,
            isNational: null,
            page: 0,
            pageSize: 10,
            sortBy: 'createdAt',
            sortDirection: 'DESC'
          }
        });
        loadEmployees();
      },

      loadPage: (params: { page: number; pageSize: number; sortBy: string; sortDirection: string }) => {
        patchState(store, {
          filters: { ...store.filters(), ...params }
        });
        loadEmployees();
      },

      setPage: (page: number) => {
        patchState(store, {
          filters: { ...store.filters(), page }
        });
        loadEmployees();
      },

      searchPersonByDocument: rxMethod<{ documentNumber: string; isNational: boolean; birthdate?: string | null }>(
        pipe(
          tap(() => patchState(store, { searchingPerson: true, personSearchError: null, foundPerson: null })),
          switchMap(({ documentNumber, isNational, birthdate }) => {
            return store._personService.findPersonByDocument(documentNumber, isNational, birthdate || undefined).pipe(
              tap((response) => {
                if (response.success && response.data) {
                  patchState(store, {
                    foundPerson: response.data,
                    searchingPerson: false,
                    personSearchError: null
                  });
                } else {
                  const message = response.message || 'No se encontró información para el documento ingresado.';
                  patchState(store, {
                    foundPerson: null,
                    searchingPerson: false,
                    personSearchError: message
                  });
                  showMessage(message, 'warn');
                }
              }),
              catchError((err) => {
                const error = err.error?.message || err.message || 'Error al buscar el documento.';
                patchState(store, {
                  foundPerson: null,
                  searchingPerson: false,
                  personSearchError: error
                });
                showMessage(error, 'error');
                return of(null);
              })
            );
          })
        )
      ),

      clearPersonSearch: () => {
        patchState(store, { foundPerson: null, searchingPerson: false, personSearchError: null });
      },

      searchEmployeeByDocument: rxMethod<string>(
        pipe(
          tap(() => patchState(store, { searchingEmployee: true, employeeSearchError: null, foundEmployee: null })),
          switchMap((documentNumber) => {
            return store._employeeService.searchByDocumentNumber(documentNumber).pipe(
              tap((response) => {
                if (response.success && response.data) {
                  patchState(store, {
                    foundEmployee: response.data,
                    searchingEmployee: false,
                    employeeSearchError: null
                  });
                } else {
                  patchState(store, {
                    foundEmployee: null,
                    searchingEmployee: false,
                    employeeSearchError: response.message || 'No se encontró empleado con el documento ingresado.'
                  });
                  handleHttpError(null, 'Error al buscar el empleado', response.message);
                }
              }),
              catchError((err) => {
                patchState(store, {
                  foundEmployee: null,
                  searchingEmployee: false,
                  employeeSearchError: null
                });
                handleHttpError(err, 'Error al buscar el empleado');
                return of(null);
              })
            );
          })
        )
      ),

      clearEmployeeSearch: () => {
        patchState(store, { foundEmployee: null, searchingEmployee: false, employeeSearchError: null });
      },

      create: rxMethod<CreateEmployeeRequest>(
        pipe(
          tap(() => patchState(store, { loading: true, error: null })),
          switchMap((request) =>
            store._employeeService.create(request).pipe(
              tap((response) => {
                if (response.success) {
                  patchState(store, { loading: false });
                  showMessage(response.message || 'Empleado creado correctamente', 'success');
                  loadEmployees();
                  loadSubsidiarySelectOptions();
                  loadPositionSelectOptions();
                } else {
                  patchState(store, { loading: false });
                  handleHttpError(null, 'Error al crear el empleado', response.message);
                }
              }),
              catchError((err) => {
                handleHttpError(err, 'Error al crear el empleado');
                return of(null);
              })
            )
          )
        )
      ),

      update: (params: { publicId: string; request: UpdateEmployeeRequest }) => {
        const { publicId, request } = params;
        return store._employeeService.update(publicId, request).pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, { successMessage: response.message || 'Empleado actualizado correctamente' });
              loadEmployees();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            const error = getErrorMessage(err, 'Error al actualizar el empleado');
            patchState(store, { error });
            return of({ success: false, message: error, data: null, timeStamp: new Date().toISOString() });
          })
        );
      },

      delete: (publicId: string) => {
        return store._employeeService.delete(publicId).pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, { successMessage: response.message || 'Empleado eliminado correctamente' });
              loadEmployees();
            } else {
              patchState(store, { error: response.message });
            }
          }),
          catchError((err) => {
            const error = getErrorMessage(err, 'Error al eliminar el empleado');
            patchState(store, { error });
            return of({ success: false, message: error, data: null, timeStamp: new Date().toISOString() });
          })
        );
      },

      refresh: () => {
        loadEmployees();
        loadSubsidiarySelectOptions();
        loadPositionSelectOptions();
        loadStateSelectOptions();
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      clearSuccess: () => {
        patchState(store, { successMessage: null });
      },

      getDetails: (publicId: string) => {
        return store._employeeService.getEmployeeForEdit(publicId);
      },

      init: () => {
        loadSubsidiarySelectOptions();
        loadPositionSelectOptions();
        loadStateSelectOptions();
        loadEmployees();
      }
    };
  })
);
