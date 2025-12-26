import { inject } from '@angular/core';
import { signalStore, withState, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { MessageService } from 'primeng/api';
import { PayrollService } from '../services/payroll.service';
import {
  PayrollListDTO,
  CreatePayrollRequest,
  PayrollPageableRequest,
  PayrollStatus,
  PayrollSummaryDTO,
  PayrollEmployeeListDTO,
  PayrollEmployeeDetailDTO
} from '@shared/types/payroll';
import { TareoListDTO } from '@shared/types/tareo';

interface PayrollState {
  payrolls: PayrollListDTO[];
  totalItems: number;
  page: number;
  size: number;
  isLoading: boolean;
  error: string | null;

  subsidiaryFilter: string | null;
  periodFilter: string | null;
  statusFilter: PayrollStatus | null;

  summary: PayrollSummaryDTO | null;
  isLoadingSummary: boolean;
  processedTareos: TareoListDTO[];
  isLoadingTareos: boolean;
  
  employees: PayrollEmployeeListDTO[];
  isLoadingEmployees: boolean;
  selectedEmployee: PayrollEmployeeDetailDTO | null;
  isLoadingEmployeeDetail: boolean;
}

const initialState: PayrollState = {
  payrolls: [],
  totalItems: 0,
  page: 0,
  size: 10,
  isLoading: false,
  error: null,
  subsidiaryFilter: null,
  periodFilter: null,
  statusFilter: null,
  summary: null,
  isLoadingSummary: false,
  processedTareos: [],
  isLoadingTareos: false,
  employees: [],
  isLoadingEmployees: false,
  selectedEmployee: null,
  isLoadingEmployeeDetail: false,
};

function mapStatusToCode(status: PayrollStatus): string {
  const statusMap: Record<PayrollStatus, string> = {
    'BORRADOR': 'PAYROLL_DRAFT',
    'CALCULANDO': 'PAYROLL_IN_PROGRESS',
    'CALCULADA': 'PAYROLL_CALCULATED',
    'CERRADA': 'PAYROLL_APPROVED',
    'ANULADA': 'PAYROLL_CANCELLED',
  };
  return statusMap[status] || status;
}

export const PayrollStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withMethods((store, payrollService = inject(PayrollService), messageService = inject(MessageService)) => {
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

    const loadPayrolls = rxMethod<void>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap(() => {
          const statusCode = store.statusFilter() ? mapStatusToCode(store.statusFilter()!) : undefined;
          
          const request: PayrollPageableRequest = {
            page: store.page(),
            size: store.size(),
            sortBy: 'createdAt',
            sortDirection: 'DESC',
            subsidiaryPublicId: store.subsidiaryFilter() || undefined,
            periodPublicId: store.periodFilter() || undefined,
            status: statusCode,
          };

          return payrollService.list(request).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, {
                  payrolls: result.data.data,
                  totalItems: result.data.totalElements,
                  isLoading: false,
                });
              } else {
                patchState(store, { isLoading: false, payrolls: [] });
                handleHttpError(null, 'Error al cargar las planillas.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar las planillas.');
              return of(null);
            })
          );
        })
      )
    );

    const createPayroll = rxMethod<CreatePayrollRequest>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((request) =>
          payrollService.create(request).pipe(
            switchMap((result) => {
              if (result.success && result.data) {
                showMessage('Planilla creada exitosamente. Iniciando cálculo...', 'success');
                return payrollService.launch(result.data.publicId).pipe(
                  tap((launchResult) => {
                    if (launchResult.success) {
                      patchState(store, { isLoading: false });
                      showMessage('Cálculo de planilla iniciado correctamente.', 'success');
                      loadPayrolls();
                    } else {
                      patchState(store, { isLoading: false });
                      handleHttpError(null, 'Error al iniciar el cálculo de la planilla.', launchResult.message);
                    }
                  }),
                  catchError((err) => {
                    patchState(store, { isLoading: false });
                    handleHttpError(err, 'Error de conexión al iniciar el cálculo de la planilla.');
                    return of(null);
                  })
                );
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al crear la planilla.', result.message);
                return of(null);
              }
            }),
            catchError((err) => {
              patchState(store, { isLoading: false });
              handleHttpError(err, 'Error de conexión al crear la planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const launchPayroll = rxMethod<string>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((publicId) =>
          payrollService.launch(publicId).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, { isLoading: false });
                showMessage('Cálculo de planilla iniciado exitosamente.', 'success');
                loadPayrolls();
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al lanzar el cálculo de la planilla.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al lanzar el cálculo de la planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const deletePayroll = rxMethod<string>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((publicId) =>
          payrollService.delete(publicId).pipe(
            tap((result) => {
              if (result.success) {
                patchState(store, { isLoading: false });
                showMessage('Planilla eliminada exitosamente.', 'success');
                loadPayrolls();
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al eliminar la planilla.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al eliminar la planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const generatePayslips = rxMethod<string>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((publicId) =>
          payrollService.generatePayslips(publicId).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, { isLoading: false });
                showMessage('Generación de boletas iniciada exitosamente.', 'success');
                loadPayrolls();
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al generar las boletas.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al generar las boletas.');
              return of(null);
            })
          )
        )
      )
    );

    const cancelPayroll = rxMethod<string>(
      pipe(
        tap(() => {
          patchState(store, { isLoading: true, error: null });
        }),
        switchMap((publicId) =>
          payrollService.cancel(publicId).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, { isLoading: false });
                showMessage('Planilla anulada exitosamente.', 'success');
                loadPayrolls();
              } else {
                patchState(store, { isLoading: false });
                handleHttpError(null, 'Error al anular la planilla.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al anular la planilla.');
              return of(null);
            })
          )
        )
      )
    );

    const getSummary = rxMethod<string>(
      pipe(
        tap(() => {
          patchState(store, { isLoadingSummary: true, error: null });
        }),
        switchMap((publicId) =>
          payrollService.getSummary(publicId).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, { summary: result.data, isLoadingSummary: false });
                getProcessedTareos(publicId);
              } else {
                patchState(store, { isLoadingSummary: false });
                handleHttpError(null, 'Error al cargar el resumen de la planilla.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar el resumen de la planilla.');
              patchState(store, { isLoadingSummary: false });
              return of(null);
            })
          )
        )
      )
    );

    const getProcessedTareos = rxMethod<string>(
      pipe(
        tap(() => {
          patchState(store, { isLoadingTareos: true });
        }),
        switchMap((publicId) =>
          payrollService.getProcessedTareos(publicId).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, { processedTareos: result.data, isLoadingTareos: false });
              } else {
                patchState(store, { isLoadingTareos: false });
              }
            }),
            catchError((err) => {
              patchState(store, { isLoadingTareos: false });
              return of(null);
            })
          )
        )
      )
    );

    const getPayrollEmployees = rxMethod<{ publicId: string; laborPublicId?: string | null; employeeDocumentNumber?: string | null }>(
      pipe(
        tap(() => {
          patchState(store, { isLoadingEmployees: true });
        }),
        switchMap(({ publicId, laborPublicId, employeeDocumentNumber }) =>
          payrollService.getPayrollEmployees(publicId, laborPublicId || undefined, employeeDocumentNumber || undefined).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, { employees: result.data, isLoadingEmployees: false });
              } else {
                patchState(store, { isLoadingEmployees: false });
                handleHttpError(null, 'Error al cargar los empleados.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar los empleados.');
              patchState(store, { isLoadingEmployees: false });
              return of(null);
            })
          )
        )
      )
    );

    const getPayrollEmployeeDetail = rxMethod<{ publicId: string; employeeDocumentNumber: string }>(
      pipe(
        tap(() => {
          patchState(store, { isLoadingEmployeeDetail: true });
        }),
        switchMap(({ publicId, employeeDocumentNumber }) =>
          payrollService.getPayrollEmployeeDetail(publicId, employeeDocumentNumber).pipe(
            tap((result) => {
              if (result.success && result.data) {
                patchState(store, { selectedEmployee: result.data, isLoadingEmployeeDetail: false });
              } else {
                patchState(store, { isLoadingEmployeeDetail: false });
                handleHttpError(null, 'Error al cargar el detalle del empleado.', result.message);
              }
            }),
            catchError((err) => {
              handleHttpError(err, 'Error de conexión al cargar el detalle del empleado.');
              patchState(store, { isLoadingEmployeeDetail: false });
              return of(null);
            })
          )
        )
      )
    );

    return {
      loadPayrolls,
      createPayroll,
      launchPayroll,
      deletePayroll,
      generatePayslips,
      cancelPayroll,
      getSummary,
      getProcessedTareos,
      getPayrollEmployees,
      getPayrollEmployeeDetail,
      setPage: (page: number) => {
        patchState(store, { page });
        loadPayrolls();
      },
      setSubsidiaryFilter: (subsidiaryPublicId: string | null) => {
        patchState(store, { subsidiaryFilter: subsidiaryPublicId, page: 0 });
        loadPayrolls();
      },
      setPeriodFilter: (periodPublicId: string | null) => {
        patchState(store, { periodFilter: periodPublicId, page: 0 });
        loadPayrolls();
      },
      setStatusFilter: (status: PayrollStatus | null) => {
        patchState(store, { statusFilter: status, page: 0 });
        loadPayrolls();
      },
      clearFilters: () => {
        patchState(store, {
          subsidiaryFilter: null,
          periodFilter: null,
          statusFilter: null,
          page: 0
        });
        loadPayrolls();
      },
      clearError: () => patchState(store, { error: null }),
      clearSummary: () => patchState(store, { summary: null, processedTareos: [] }),
      clearEmployeeDetail: () => patchState(store, { selectedEmployee: null }),
      init: () => {
        loadPayrolls();
      },
    };
  })
);