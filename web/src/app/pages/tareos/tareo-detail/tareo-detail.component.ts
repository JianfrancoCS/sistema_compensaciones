import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TareoStore } from '@core/store/tareo.store';
import { TareoDetailDTO } from '@shared/types/tareo';
import { ButtonModule } from 'primeng/button';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-tareo-detail',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule
  ],
  templateUrl: './tareo-detail.component.html',
  styleUrl: './tareo-detail.component.css'
})
export class TareoDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private tareoStore = inject(TareoStore);

  tareoDetail = signal<TareoDetailDTO | null>(null);
  loadingDetail = signal(true);

  ngOnInit(): void {
    const publicId = this.route.snapshot.paramMap.get('publicId');
    if (publicId) {
      this.loadTareoDetail(publicId);
    } else {
      this.goBack();
    }
  }

  async loadTareoDetail(publicId: string): Promise<void> {
    this.loadingDetail.set(true);
    try {
      const result = await firstValueFrom(this.tareoStore.getDetail(publicId));
      this.loadingDetail.set(false);
      if (result?.success && result.data) {
        this.tareoDetail.set(result.data);
      } else {
        this.goBack();
      }
    } catch (error) {
      this.loadingDetail.set(false);
      this.goBack();
    }
  }

  goBack(): void {
    this.router.navigate(['/system/tareos']);
  }

  formatTime(timeStr: string | null): string {
    if (!timeStr) return '-';
    return timeStr.substring(0, 5); // HH:mm
  }
}

