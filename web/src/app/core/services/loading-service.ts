import {computed, Injectable, signal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private _isLoading = signal<boolean>(false);
  private _loadingCount = signal<number>(0);
  private _loadingText = signal<string>('Cargando...');

  isLoading = this._isLoading.asReadonly();
  loadingCount = this._loadingCount.asReadonly();
  loadingText = this._loadingText.asReadonly();

  hasActiveLoading = computed(() => this._loadingCount() > 0);

  constructor() {}

  show(text?: string): void {
    const currentCount = this._loadingCount();
    this._loadingCount.set(currentCount + 1);
    this._isLoading.set(true);

    if (text) {
      this._loadingText.set(text);
    }
  }

  hide(): void {
    const currentCount = this._loadingCount();
    if (currentCount > 0) {
      const newCount = currentCount - 1;
      this._loadingCount.set(newCount);
      this._isLoading.set(newCount > 0);
    }
  }

  forceHide(): void {
    this._loadingCount.set(0);
    this._isLoading.set(false);
  }

  setLoadingText(text: string): void {
    this._loadingText.set(text);
  }

  resetText(): void {
    this._loadingText.set('Cargando...');
  }

  getStatus() {
    return {
      isLoading: this._isLoading(),
      count: this._loadingCount(),
      text: this._loadingText()
    };
  }
}
