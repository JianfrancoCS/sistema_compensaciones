import { inject } from '@angular/core';
import { signalStore, withState, withMethods, patchState, withProps } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { MessageService } from 'primeng/api';
import { PayrollConfigurationService } from '../services/payroll-configuration.service';
import { ConceptService } from '../services/concept.service';
import { PayrollConfigurationConceptAssignmentDTO, PayrollConfigurationConceptAssignmentListApiResult, UpdateConceptAssignmentsRequest, CommandPayrollConfigurationResponse, PayrollConfigurationApiResult, CreatePayrollConfigurationRequest, VoidApiResult } from '@shared/types/payroll-configuration';
import { ApiResult } from '@shared/types/api';
import { ConceptSelectOptionDTO, ConceptSelectOptionListApiResult } from '@shared/types/concept';

interface PayrollConfigurationState {
  conceptAssignments: PayrollConfigurationConceptAssignmentDTO[];
  activeConfiguration: CommandPayrollConfigurationResponse | null;
  availableConcepts: ConceptSelectOptionDTO[];
  isLoading: boolean;
  error: string | null;
}

const initialState: PayrollConfigurationState = {
  conceptAssignments: [],
  activeConfiguration: null,
  availableConcepts: [],
  isLoading: false,
  error: null,
};

export const PayrollConfigurationStore = signalStore(
  withState<PayrollConfigurationState>(initialState),

  withProps(() => ({
    _payrollConfigurationService: inject(PayrollConfigurationService),
    _conceptService: inject(ConceptService),
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
      console.log('Store state after error:', store.isLoading(), store.error());
    };

    const _loadConceptAssignments = rxMethod<void>(
      pipe(
        tap(() => {
          console.log('_loadConceptAssignments: Iniciando carga de conceptos asignados.');
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap(() =>
          store._payrollConfigurationService.getConceptAssignmentsForActiveConfiguration().pipe(
            tap((result: PayrollConfigurationConceptAssignmentListApiResult) => {
              console.log('_loadConceptAssignments: Resultado del servicio:', result);
              if (result.success && result.data) {
                patchState(store, {
                  conceptAssignments: result.data,
                  isLoading: false,
                });
                console.log('_loadConceptAssignments: Conceptos asignados cargados exitosamente.', store.conceptAssignments());
              } else {
                patchState(store, { isLoading: false, conceptAssignments: [] });
                handleHttpError(null, 'Error al cargar los conceptos de configuración de planilla.', result.message);
              }
            }),
            catchError((err) => {
              patchState(store, { isLoading: false, conceptAssignments: [] });
              handleHttpError(err, 'Error de conexión al cargar los conceptos de configuración de planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const loadAvailableConcepts = rxMethod<void>(
      pipe(
        tap(() => {
          console.log('loadAvailableConcepts: Iniciando carga de conceptos disponibles.');
        }),
        switchMap(() =>
          store._conceptService.getSelectOptions().pipe(
            tap((result: ConceptSelectOptionListApiResult) => {
              console.log('loadAvailableConcepts: Resultado del servicio:', result);
              if (result.success && result.data) {
                patchState(store, {
                  availableConcepts: result.data,
                });
                console.log('loadAvailableConcepts: Conceptos disponibles cargados exitosamente.', store.availableConcepts());
              } else {
                handleHttpError(null, 'Error al cargar los conceptos disponibles.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar los conceptos disponibles.');
              return of(null);
            })
          )
        )
      )
    );

    const loadActivePayrollConfiguration = rxMethod<void>(
      pipe(
        tap(() => {
          console.log('loadActivePayrollConfiguration: Iniciando carga de configuración activa.');
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap(() =>
          store._payrollConfigurationService.getActivePayrollConfiguration().pipe(
            tap((result: PayrollConfigurationApiResult) => {
              console.log('loadActivePayrollConfiguration: Resultado del servicio:', result);
              if (result.success) {
                patchState(store, {
                  activeConfiguration: result.data || null,
                  isLoading: false,
                });
                if (result.data) {
                  console.log('loadActivePayrollConfiguration: Configuración activa cargada exitosamente.', store.activeConfiguration());
                  _loadConceptAssignments();
                } else {
                  console.log('loadActivePayrollConfiguration: No se encontró configuración activa de planilla (resultado exitoso con data: null).');
                  patchState(store, { conceptAssignments: [] });
                }
              } else {
                patchState(store, { isLoading: false, activeConfiguration: null, conceptAssignments: [] });
                handleHttpError(null, 'Error al cargar la configuración activa de planilla.', result.message);
              }
            }),
            catchError((err) => {
              patchState(store, { isLoading: false, activeConfiguration: null, conceptAssignments: [] });
              handleHttpError(err, 'Error de conexión al cargar la configuración activa de planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const createPayrollConfiguration = rxMethod<CreatePayrollConfigurationRequest>(
      pipe(
        tap(() => {
          console.log('createPayrollConfiguration: Iniciando creación de configuración.');
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((request) =>
          store._payrollConfigurationService.createPayrollConfiguration(request).pipe(
            tap((result: PayrollConfigurationApiResult) => {
              console.log('createPayrollConfiguration: Resultado del servicio:', result);
              if (result.success && result.data) {
                patchState(store, {
                  activeConfiguration: result.data,
                  isLoading: false,
                });
                showMessage('Configuración de planilla creada exitosamente.', 'success');
                loadActivePayrollConfiguration(); // Recargar la configuración activa y sus conceptos
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al crear la configuración de planilla.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al crear la configuración de planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const deleteActivePayrollConfiguration = rxMethod<void>(
      pipe(
        tap(() => {
          console.log('deleteActivePayrollConfiguration: Iniciando eliminación de configuración.');
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap(() =>
          store._payrollConfigurationService.deleteActivePayrollConfiguration().pipe(
            tap((result: VoidApiResult) => {
              console.log('deleteActivePayrollConfiguration: Resultado del servicio:', result);
              if (result.success) {
                patchState(store, {
                  activeConfiguration: null,
                  conceptAssignments: [],
                  isLoading: false,
                });
                showMessage('Configuración de planilla eliminada exitosamente.', 'success');
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al eliminar la configuración de planilla.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al eliminar la configuración de planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const updateConceptAssignments = rxMethod<UpdateConceptAssignmentsRequest>(
      pipe(
        tap(() => {
          console.log('updateConceptAssignments: Iniciando actualización de conceptos.');
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((request) =>
          store._payrollConfigurationService.updateConceptAssignmentsForActiveConfiguration(request).pipe(
            tap((result: PayrollConfigurationConceptAssignmentListApiResult) => {
              console.log('updateConceptAssignments: Resultado del servicio:', result);
              if (result.success && result.data) {
                patchState(store, {
                  conceptAssignments: result.data,
                  isLoading: false,
                });
                showMessage('Conceptos de configuración de planilla actualizados exitosamente.', 'success');
                loadActivePayrollConfiguration(); // <<-- AÑADIDO AQUÍ: Recargar la configuración activa para reflejar los cambios
                console.log('updateConceptAssignments: Conceptos actualizados exitosamente.', store.conceptAssignments());
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al actualizar los conceptos de configuración de planilla.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al actualizar los conceptos de configuración de planilla.');
              return of(null);
            })
          )
        )
      )
    );

    return {
      loadConceptAssignments: _loadConceptAssignments,
      loadActivePayrollConfiguration,
      loadAvailableConcepts,
      createPayrollConfiguration,
      deleteActivePayrollConfiguration,
      updateConceptAssignments,
      clearError: () => patchState(store, { error: null }),
      init: () => {
        console.log('PayrollConfigurationStore: init() llamado.');
        loadActivePayrollConfiguration();
        loadAvailableConcepts();
      },
    };
  })
);
