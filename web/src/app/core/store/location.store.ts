import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of } from 'rxjs';
import { SelectOption } from '@shared/types/api';
import { LocationService } from '../services/location.service';
import { MessageService } from 'primeng/api';
import { DistrictDetailResponseDTO } from '@shared/types/location';

interface LocationState {
  departments: SelectOption[];
  provinces: SelectOption[];
  districts: SelectOption[];
  loading: boolean;
  error: string | null;
}

const initialState: LocationState = {
  departments: [],
  provinces: [],
  districts: [],
  loading: false,
  error: null,
};

export const LocationStore = signalStore(
  withState<LocationState>(initialState),

  withProps(() => ({
    _locationService: inject(LocationService),
    _messageService: inject(MessageService)
  })),

  withComputed((state) => ({
    hasDepartments: computed(() => state.departments().length > 0),
    hasProvinces: computed(() => state.provinces().length > 0),
    hasDistricts: computed(() => state.districts().length > 0),
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

    const loadDepartments = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => store._locationService.getDepartments().pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, { departments: response.data, loading: false });
            } else {
              patchState(store, { loading: false });
              handleHttpError(null, 'Error al cargar departamentos', response.message);
            }
          }),
          catchError((err) => {
            handleHttpError(err, 'Error de conexión al cargar departamentos');
            return of(null);
          })
        ))
      )
    );

    const loadProvinces = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((departmentId) => store._locationService.getProvincesByDepartmentId(departmentId).pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, { provinces: response.data, loading: false });
            } else {
              patchState(store, { loading: false });
              handleHttpError(null, 'Error al cargar provincias', response.message);
            }
          }),
          catchError((err) => {
            handleHttpError(err, 'Error de conexión al cargar provincias');
            return of(null);
          })
        ))
      )
    );

    const loadDistricts = rxMethod<string>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((provinceId) => store._locationService.getDistrictsByProvinceId(provinceId).pipe(
          tap((response) => {
            if (response.success) {
              patchState(store, { districts: response.data, loading: false });
            } else {
              patchState(store, { loading: false });
              handleHttpError(null, 'Error al cargar distritos', response.message);
            }
          }),
          catchError((err) => {
            handleHttpError(err, 'Error de conexión al cargar distritos');
            return of(null);
          })
        ))
      )
    );

    return {
      departments: store.departments,
      provinces: store.provinces,
      districts: store.districts,
      loading: store.loading,
      error: store.error,
      loadDepartments,
      loadProvinces,
      loadDistricts,
      resetProvinces: () => patchState(store, { provinces: [], districts: [] }),
      resetDistricts: () => patchState(store, { districts: [] }),
      clearError: () => patchState(store, { error: null }),
      getDistrictDetails: (districtId: string) => {
        return store._locationService.getDistrictDetails(districtId);
      }
    };
  })
);
