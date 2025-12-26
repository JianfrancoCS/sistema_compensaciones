import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { PayslipService } from '@core/services/payslip.service';
import { catchError, finalize } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-payslip-viewer',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    ProgressSpinnerModule
  ],
  templateUrl: './payslip-viewer.component.html',
})
export class PayslipViewerComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private payslipService = inject(PayslipService);
  private sanitizer = inject(DomSanitizer);

  payslipPublicId: string | null = null;
  pdfUrl: SafeResourceUrl | null = null;
  isLoading = false;
  error: string | null = null;
  private blobUrl: string | null = null;

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.payslipPublicId = params['id'];
      if (this.payslipPublicId) {
        this.loadPdf();
      }
    });
  }

  loadPdf() {
    if (!this.payslipPublicId) return;

    this.isLoading = true;
    this.error = null;
    
    if (this.blobUrl) {
      URL.revokeObjectURL(this.blobUrl);
      this.blobUrl = null;
    }
    
    this.payslipService.getPdfAsBlob(this.payslipPublicId)
      .pipe(
        catchError((err) => {
          this.error = 'Error al cargar el PDF. Por favor, intenta nuevamente.';
          console.error('Error loading PDF:', err);
          return of(null);
        }),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe((blob) => {
        if (blob) {
          this.blobUrl = URL.createObjectURL(blob);
          this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.blobUrl);
        }
      });
  }

  goBack() {
    this.router.navigate(['/system/payslips']);
  }

  ngOnDestroy() {
    if (this.blobUrl) {
      URL.revokeObjectURL(this.blobUrl);
      this.blobUrl = null;
    }
  }
}

