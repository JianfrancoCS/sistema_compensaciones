import {Component, inject} from '@angular/core';
import {LoadingService} from '../../../core/services/loading-service';

@Component({
  selector: 'app-overlay-loading',
  imports: [],
  templateUrl: './overlay-loading.html',
  standalone: true,
  styleUrl: './overlay-loading.css'
})
export class OverlayLoading {
  loadingService = inject(LoadingService);

  constructor() {
  }

  forceClose(): void {
    this.loadingService.forceHide();
  }
}
