import { inject } from '@angular/core';
import { signalStore, withState, withMethods, withProps, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, map, catchError, of, Observable, tap, switchMap, shareReplay, finalize } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '@env/environment';
import { AuthService } from '../services/auth.service';

interface InternalFileState {
  blobUrls: Record<string, string>;
  loadingUrls: string[];
}

const initialState: InternalFileState = {
  blobUrls: {},
  loadingUrls: [],
};

export const InternalFileStore = signalStore(
  { providedIn: 'root' },
  withState<InternalFileState>(initialState),

  withProps(() => ({
    _http: inject(HttpClient),
    _apiUrl: environment.apiUrl,
    _authService: inject(AuthService),
    _loadingObservables: new Map<string, Observable<string | null>>(),
  })),

  withMethods((store) => {
    const getAuthenticatedUrl = (url: string | null | undefined): string | null => {
      if (!url) return null;

      if (url.startsWith('blob:')) {
        return url;
      }

      if (url.startsWith('http://') || url.startsWith('https://')) {
        if (!url.startsWith(store._apiUrl)) {
          return url;
        }
      }

      const absoluteUrl = convertToAbsoluteUrl(url, store._apiUrl);
      
      const token = store._authService.getToken();
      if (!token) {
        return absoluteUrl;
      }

      const separator = absoluteUrl.includes('?') ? '&' : '?';
      return `${absoluteUrl}${separator}token=${encodeURIComponent(token)}`;
    };

    const getBlobUrl = (url: string | null | undefined): string | null => {
      return getAuthenticatedUrl(url);
    };

    const preloadBlobUrl = rxMethod<string | null | undefined>(
      pipe(
        switchMap((url) => {
          const authenticatedUrl = getAuthenticatedUrl(url);
          return of(authenticatedUrl);
        })
      )
    );

    const loadBlobUrl = (url: string | null | undefined): void => {
    };

    const isLoading = (url: string | null | undefined): boolean => {
      if (!url) return false;
      return store.loadingUrls().includes(url);
    };

    const revokeBlobUrl = (blobUrl: string | null) => {
      if (blobUrl && blobUrl.startsWith('blob:')) {
        URL.revokeObjectURL(blobUrl);
        const currentBlobUrls = { ...store.blobUrls() };
        const urlToRemove = Object.keys(currentBlobUrls).find(
          (key) => currentBlobUrls[key] === blobUrl
        );
        if (urlToRemove) {
          delete currentBlobUrls[urlToRemove];
          patchState(store, { blobUrls: currentBlobUrls });
        }
      }
    };

    const clearAllBlobUrls = () => {
      const blobUrls = store.blobUrls();
      Object.values(blobUrls).forEach((blobUrl) => {
        if (blobUrl && blobUrl.startsWith('blob:')) {
          URL.revokeObjectURL(blobUrl);
        }
      });
      store._loadingObservables.clear();
      patchState(store, { blobUrls: {}, loadingUrls: [] });
    };

    const getBlobUrlObservable = (
      url: string | null | undefined
    ): Observable<string | null> => {
      if (!url) {
        return of(null);
      }

      if (url.startsWith('blob:')) {
        return of(url);
      }

      if (url.startsWith('http://') || url.startsWith('https://')) {
        if (!url.startsWith(store._apiUrl)) {
          return of(url);
        }
      }

      const blobUrls = store.blobUrls();
      if (blobUrls[url]) {
        return of(blobUrls[url]);
      }

      const existingObservable = store._loadingObservables.get(url);
      if (existingObservable) {
        return existingObservable;
      }

      const loadingUrls = store.loadingUrls();
      if (!loadingUrls.includes(url)) {
        patchState(store, { loadingUrls: [...loadingUrls, url] });
      }

      const absoluteUrl = convertToAbsoluteUrl(url, store._apiUrl);

      const loadingObservable = store._http
        .get(absoluteUrl, {
          responseType: 'blob',
          headers: new HttpHeaders({
            Accept: 'application/pdf,image/*,application/octet-stream,*/*',
          }),
        })
        .pipe(
          map((blob) => {
            const blobUrl = URL.createObjectURL(blob);
            const currentBlobUrls = { ...store.blobUrls() };
            currentBlobUrls[url] = blobUrl;
            patchState(store, { blobUrls: currentBlobUrls });
            return blobUrl;
          }),
          catchError((err) => {
            console.error('Error al descargar archivo autenticado:', err);
            return of(null);
          }),
          shareReplay(1),
          finalize(() => {
            store._loadingObservables.delete(url);
            const updatedLoading = store.loadingUrls().filter((u) => u !== url);
            patchState(store, { loadingUrls: updatedLoading });
          })
        );

      store._loadingObservables.set(url, loadingObservable);
      return loadingObservable;
    };

    return {
      getBlobUrl,
      getAuthenticatedUrl,
      preloadBlobUrl,
      loadBlobUrl,
      isLoading,
      revokeBlobUrl,
      clearAllBlobUrls,
      getBlobUrlObservable,
    };
  })
);

function convertToAbsoluteUrl(url: string, apiUrl: string): string {
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  if (url.startsWith('/')) {
    return `${apiUrl}${url}`;
  }
  return `${apiUrl}/${url}`;
}

