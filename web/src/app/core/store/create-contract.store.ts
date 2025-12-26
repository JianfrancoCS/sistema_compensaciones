import { inject, computed } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, of, forkJoin } from 'rxjs';
import { ContractService } from '../services/contract.service';
import { ContractTypeService } from '../services/contract-type.service';
import { ContractTemplateService } from '../services/contract-template.service';
import { SubsidiaryService } from '../services/subsidiary.service';
import { AreaService } from '../services/area.service';
import { PositionService } from '../services/position.service';
import { PersonStore } from './person.store';
import { SelectOption } from '@shared/types/api';
import { ContractVariableWithValidation } from '@shared/types/variable';
import { CreateContractRequest } from '@shared/types/contract';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { SubsidiarySignerService } from '../services/subsidiary-signer.service';

export interface CreateContractState {
  loading: boolean;
  submitting: boolean;
  error: string | null;

  contractTypes: SelectOption[];
  templates: SelectOption[];
  subsidiaries: SelectOption[];
  areas: SelectOption[];
  positions: SelectOption[];
  templateVariables: ContractVariableWithValidation[];

  selectedContractType: SelectOption | null;
  selectedArea: SelectOption | null;

  showEndDate: boolean;
  
  createdContract: any | null;
}

const initialState: CreateContractState = {
  loading: false,
  submitting: false,
  error: null,
  contractTypes: [],
  templates: [],
  subsidiaries: [],
  areas: [],
  positions: [],
  templateVariables: [],
  selectedContractType: null,
  selectedArea: null,
  showEndDate: false,
  createdContract: null
};

export const CreateContractStore = signalStore(
  withState<CreateContractState>(initialState),
  withComputed((store) => ({
    createdContractSignal: computed(() => store.createdContract())
  })),
  withMethods((store) => {
    const contractService = inject(ContractService);
    const contractTypeService = inject(ContractTypeService);
    const contractTemplateService = inject(ContractTemplateService);
    const subsidiaryService = inject(SubsidiaryService);
    const areaService = inject(AreaService);
    const positionService = inject(PositionService);
    const personStore = inject(PersonStore);
    const messageService = inject(MessageService);
    const router = inject(Router);
    const subsidiarySignerService = inject(SubsidiarySignerService);

    const showToast = (severity: 'success' | 'error' | 'info' | 'warn', summary: string, detail: string) => {
      messageService.add({ severity, summary, detail });
    };

    const getErrorMessage = (err: any, defaultMessage: string): string => {
      let errorMessage = defaultMessage;
      if (err?.error && typeof err.error === 'object' && 'message' in err.error) {
        errorMessage = err.error.message;
      } else if (err?.message) {
        errorMessage = err.message;
      }
      showToast('error', 'Error', errorMessage); // Show toast for errors
      return errorMessage;
    };

    const loadInitialData = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() => {
          return forkJoin({
            contractTypes: contractTypeService.getContractTypeSelectOptions(),
            subsidiaries: subsidiaryService.getSelectOptions(),
            areas: areaService.getSelectOptions()
          }).pipe(
            tap((responses) => {
              patchState(store, {
                contractTypes: responses.contractTypes.success ? responses.contractTypes.data : [],
                subsidiaries: responses.subsidiaries.success ? responses.subsidiaries.data : [],
                areas: responses.areas.success ? responses.areas.data : [],
                loading: false
              });
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar datos iniciales');
              patchState(store, { loading: false, error });
              return of(null);
            })
          );
        })
      )
    );

    const loadTemplatesByType = rxMethod<string>(
      pipe(
        switchMap((contractTypeId) => {
          return contractTemplateService.getContractTemplateSelectOptions(contractTypeId).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { templates: response.data });
              } else {
                getErrorMessage({ message: response.message }, 'Error al cargar plantillas'); // Use getErrorMessage
                patchState(store, { error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar plantillas');
              patchState(store, { error });
              return of(null);
            })
          );
        })
      )
    );

    const loadPositionsByArea = rxMethod<string>(
      pipe(
        switchMap((areaId) => {
          return positionService.getPositionsSelectOptions(areaId).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { positions: response.data });
              } else {
                getErrorMessage({ message: response.message }, 'Error al cargar cargos'); // Use getErrorMessage
                patchState(store, { error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar cargos');
              patchState(store, { error });
              return of(null);
            })
          );
        })
      )
    );

    const loadVariablesByTemplate = rxMethod<string>(
      pipe(
        switchMap((templateId) => {
          return contractTemplateService.getVariablesWithValidation(templateId).pipe(
            tap((response) => {
              if (response.success) {
                patchState(store, { templateVariables: response.data });
              } else {
                getErrorMessage({ message: response.message }, 'Error al cargar variables'); // Use getErrorMessage
                patchState(store, { error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al cargar variables');
              patchState(store, { error });
              return of(null);
            })
          );
        })
      )
    );

    const createContract = rxMethod<{ request: CreateContractRequest; photo: File }>(
      pipe(
        tap(() => patchState(store, { submitting: true, error: null })),
        switchMap(({ request, photo }) => {
          return contractService.create(request, photo).pipe(
            tap((response) => {
              patchState(store, { submitting: false });
              if (response.success) {
                showToast('success', 'Éxito', 'Contrato creado correctamente. Ahora puedes firmarlo.');
                patchState(store, { createdContract: response.data });
              } else {
                showToast('error', 'Error', response.message || 'Error al crear el contrato');
                patchState(store, { error: response.message });
              }
            }),
            catchError((err) => {
              const error = getErrorMessage(err, 'Error al crear el contrato');
              patchState(store, { submitting: false, error });
              return of(null);
            })
          );
        })
      )
    );

    return {
      init: () => {
        loadInitialData();
      },

      selectContractType: (contractType: SelectOption) => {
        patchState(store, {
          selectedContractType: contractType,
          templates: [],
          templateVariables: [],
          showEndDate: contractType.name.toLowerCase().includes('plazo') ||
                      contractType.name.toLowerCase().includes('fijo') ||
                      contractType.name.toLowerCase().includes('temporal')
        });
        loadTemplatesByType(contractType.publicId);
      },

      selectArea: (area: SelectOption) => {
        patchState(store, {
          selectedArea: area,
          positions: []
        });
        loadPositionsByArea(area.publicId);
      },

      selectTemplate: (templateId: string) => {
        loadVariablesByTemplate(templateId);
      },

      create: createContract,

      searchPerson: (documentNumber: string) => {
        personStore.searchByDocument(documentNumber);
      },

      clearError: () => {
        patchState(store, { error: null });
      },

      reset: () => {
        patchState(store, initialState);
      },

      clearCreatedContract: () => {
        patchState(store, { createdContract: null });
      },

      showToast,
      getErrorMessage,

      isSearchingPerson: () => personStore.isSearching(),
      personFound: () => personStore.personFound(),
      personNotFound: () => personStore.personNotFound(),
      foundPersonData: () => personStore.foundPerson(),
      clearPersonSearch: () => {
        personStore.clearSearch();
      },

      validateSubsidiarySigner: (subsidiaryPublicId: string) => {
        return subsidiarySignerService.getSignerBySubsidiary(subsidiaryPublicId).pipe(
          tap((response) => {
            if (response.success) {
              if (!response.data?.signatureImageUrl) {
                showToast('warn', 'Firma requerida', 
                  'El responsable de firma asignado no tiene una imagen de firma. Por favor, asigne una firma antes de crear el contrato.');
              }
            }
          }),
          catchError((error) => {
            let errorMessage = error?.error?.message || 
              'No se encontró un responsable de firma asignado para este fundo. Por favor, asigne un encargado antes de crear el contrato.';
            
            if (errorMessage.includes('exception.') && errorMessage.length < 100) {
              errorMessage = 'No se encontró un responsable de firma asignado para este fundo. Por favor, asigne un encargado antes de crear el contrato.';
            }
            
            showToast('error', 'Fundo sin responsable', errorMessage);
            return of(null);
          })
        );
      }
    };
  })
);
