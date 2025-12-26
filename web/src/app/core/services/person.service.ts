import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'environments/environment';
import { ApiResult } from '@shared/types/api';
import { PersonDetailsDTO } from '@shared/types/person';

export type { PersonDetailsDTO };

@Injectable({
  providedIn: 'root'
})
export class PersonService {
  private readonly _http = inject(HttpClient);
  private readonly _url = `${environment.apiUrl}/v1/persons`;

  findPersonByDocument(documentNumber: string, isNational?: boolean, birthdate?: string | null): Observable<ApiResult<PersonDetailsDTO | null>> {
    let url = `${this._url}/${documentNumber}`;
    const params: string[] = [];
    
    if (isNational !== undefined) {
      params.push(`isNational=${isNational}`);
    }
    
    if (birthdate) {
      params.push(`birthdate=${birthdate}`);
    }
    
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }
    
    return this._http.get<ApiResult<PersonDetailsDTO | null>>(url).pipe(
      catchError(err => {
        console.error('Error fetching person data:', err);
        const errorMessage = err.error?.message || err.message || 'No se pudo encontrar la persona.';
        return of({ success: false, message: errorMessage, data: null, timeStamp: new Date().toISOString() });
      })
    );
  }
}
