import { MessageService } from 'primeng/api';
import { ApiResult } from '../models/api.model';

export function handleApiResponse(
  response: ApiResult<any>,
  messageService: MessageService,
  successMessage?: string
): void {
  if (response.success) {
    messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: successMessage || response.message
    });
  } else {
    messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: response.message || 'Ha ocurrido un error desconocido'
    });
  }
}

export function handleApiError(
  err: any,
  messageService: MessageService,
  defaultMessage: string
): void {
  messageService.add({
    severity: 'error',
    summary: 'Error',
    detail: err.error?.message || err.message || defaultMessage
  });
}