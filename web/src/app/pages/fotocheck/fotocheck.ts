import { Component, inject, OnInit, signal, computed, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, EmployeeMeResponse } from '@core/services/auth.service';
import { CompanyStore } from '@core/store/company.store';
import { QrCodeComponent } from 'ng-qrcode';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import html2canvas from 'html2canvas';

@Component({
  selector: 'app-fotocheck',
  standalone: true,
  imports: [
    CommonModule,
    QrCodeComponent,
    ButtonModule,
    ToastModule
  ],
  templateUrl: './fotocheck.html',
  styleUrls: ['./fotocheck.css'],
  providers: [MessageService]
})
export class FotocheckComponent implements OnInit {
  @ViewChild('fotocheckCard', { static: false }) fotocheckCard!: ElementRef<HTMLDivElement>;

  private authService = inject(AuthService);
  private companyStore = inject(CompanyStore);
  private messageService = inject(MessageService);

  userInfo = signal<EmployeeMeResponse | null>(null);
  loading = signal(false);
  photoError = signal(false);
  companyLogoUrl = computed(() => this.companyStore.company()?.logoUrl);

  ngOnInit(): void {
    this.companyStore.init();
    this.loadUserInfo();
  }

  loadUserInfo(): void {
    this.loading.set(true);
    this.authService.getMyInfo().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.userInfo.set(response.data);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: response.message || 'No se pudo cargar la información del usuario'
          });
        }
        this.loading.set(false);
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar la información del usuario'
        });
        this.loading.set(false);
      }
    });
  }

  get fullName(): string {
    const info = this.userInfo();
    if (!info) return '-';
    
    const names = (info.names || '').trim();
    const paternalLastname = (info.paternalLastname || '').trim();
    const maternalLastname = (info.maternalLastname || '').trim();
    
    const parts: string[] = [];
    
    if (names) {
      parts.push(names);
    }
    if (paternalLastname) {
      parts.push(paternalLastname);
    }
    if (maternalLastname) {
      parts.push(maternalLastname);
    }
    
    const fullName = parts.join(' ');
    const words = fullName.split(' ').filter(w => w.length > 0);
    
    const seen = new Set<string>();
    const uniqueWords: string[] = [];
    
    for (const word of words) {
      const wordLower = word.toUpperCase();
      if (!seen.has(wordLower)) {
        seen.add(wordLower);
        uniqueWords.push(word);
      }
    }
    
    return uniqueWords.length > 0 ? uniqueWords.join(' ') : '-';
  }

  get documentNumber(): string | null {
    const info = this.userInfo();
    return info?.documentNumber || null;
  }

  photoUrl = computed(() => {
    const info = this.userInfo();
    const url = info?.photoUrl || null;
    if (url && this.photoError()) {
      this.photoError.set(false);
    }
    return url;
  });

  get isAdmin(): boolean {
    const info = this.userInfo();
    return !info?.documentNumber;
  }

  get qrValue(): string {
    if (this.isAdmin || !this.documentNumber) {
      return '';
    }
    return this.documentNumber;
  }

  getInitials(): string {
    const info = this.userInfo();
    if (!info) return '?';
    
    const names = info.names || '';
    const paternal = info.paternalLastname || '';
    
    if (names && paternal) {
      return (names.charAt(0) + paternal.charAt(0)).toUpperCase();
    } else if (names) {
      return names.charAt(0).toUpperCase();
    }
    
    return '?';
  }

  onPhotoError(event: Event): void {
    this.photoError.set(true);
  }

  onLogoError(event: Event): void {
  }

  async printFotocheck(): Promise<void> {
    const cardElement = this.fotocheckCard?.nativeElement;
    if (!cardElement) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo encontrar el elemento del fotocheck'
      });
      return;
    }

    try {
      this.messageService.add({
        severity: 'info',
        summary: 'Preparando impresión',
        detail: 'Generando imagen del fotocheck...',
        life: 2000
      });

      const canvas = await html2canvas(cardElement, {
        backgroundColor: '#ffffff',
        scale: 1.5,
        logging: false,
        useCORS: true,
        allowTaint: true,
        width: cardElement.offsetWidth,
        height: cardElement.offsetHeight
      });

      const imgData = canvas.toDataURL('image/png');

      const printWindow = window.open('', '_blank');
      if (!printWindow) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'No se pudo abrir la ventana de impresión. Por favor, permite ventanas emergentes.'
        });
        return;
      }

      printWindow.document.write(`
        <!DOCTYPE html>
        <html>
          <head>
            <title>Fotocheck - Imprimir</title>
            <style>
              * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
              }
              body {
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                background: white;
              }
              img {
                max-width: 400px;
                width: 100%;
                height: auto;
                display: block;
                margin: 0 auto;
              }
              @media print {
                body {
                  margin: 0;
                  padding: 0;
                  display: flex;
                  justify-content: center;
                  align-items: center;
                  min-height: 100vh;
                }
                img {
                  max-width: 400px;
                  width: auto;
                  height: auto;
                }
                @page {
                  size: auto;
                  margin: 0.5cm;
                }
              }
            </style>
          </head>
          <body>
            <img src="${imgData}" alt="Fotocheck" />
            <script>
              window.onload = function() {
                window.print();
                window.onafterprint = function() {
                  window.close();
                };
              };
            </script>
          </body>
        </html>
      `);
      printWindow.document.close();
    } catch (error) {
      console.error('Error al imprimir fotocheck:', error);
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Ocurrió un error al generar la impresión del fotocheck'
      });
    }
  }
}

