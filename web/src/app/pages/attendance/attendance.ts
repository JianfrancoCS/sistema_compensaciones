import { Component, inject, OnInit, signal, effect, computed } from '@angular/core';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ToastModule } from 'primeng/toast';
import { ButtonModule } from 'primeng/button';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { TabsModule } from 'primeng/tabs';
import { AttendanceStore } from '@core/store/attendance.store';
import { SubsidiaryStore } from '@core/store/subsidiary.store';
import { ContinuousBarcodeScannerComponent } from './components/continuous-barcode-scanner/continuous-barcode-scanner.component';

interface RecentRecord {
  documentNumber: string;
  entryType: string;
  time: string;
  markingReason: string;
  timestamp: number;
}

@Component({
  selector: 'app-attendance',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    SelectModule,
    ToastModule,
    ButtonModule,
    ToggleSwitchModule,
    TabsModule,
    ContinuousBarcodeScannerComponent
  ],
  templateUrl: './attendance.html',
  styleUrls: ['./attendance.css'],
  providers: [MessageService]
})
export class AttendanceComponent implements OnInit {
  private fb = inject(FormBuilder);
  private attendanceStore = inject(AttendanceStore);
  private subsidiaryStore = inject(SubsidiaryStore);
  private messageService = inject(MessageService);

  readonly subsidiaries = this.subsidiaryStore.selectOptions;
  readonly markingReasons = this.attendanceStore.markingReasons;
  readonly externalMarkingReasons = this.attendanceStore.externalMarkingReasons;
  readonly loading = this.attendanceStore.loading;
  readonly lastMarkingResponse = this.attendanceStore.lastMarkingResponse;

  scannerMode = signal<'qr' | 'manual'>('qr');
  isExternalMarking = signal(false);
  recentRecords = signal<RecentRecord[]>([]);
  activeTab = signal<string>('0');

  entryRecords = computed(() => this.recentRecords().filter(r => r.entryType === 'ENTRADA'));
  exitRecords = computed(() => this.recentRecords().filter(r => r.entryType === 'SALIDA'));

  subsidiaryControl = new FormControl('', Validators.required);
  markingReasonControl = new FormControl('', Validators.required);

  attendanceForm = this.fb.group({
    personDocumentNumber: ['', Validators.required],
    subsidiaryPublicId: ['', Validators.required],
    markingReasonPublicId: ['', Validators.required],
    entryType: [true]
  });

  private lastScanTimestamp = 0;
  private readonly SCAN_DEBOUNCE_MS = 500;

  constructor() {
    this.attendanceStore.init();
    this.subsidiaryStore.init();

    effect(() => {
      const subsidiary = this.subsidiaryControl.value;
      if (subsidiary) {
        this.attendanceForm.patchValue({ subsidiaryPublicId: subsidiary }, { emitEvent: false });
      }
    });

    effect(() => {
      const markingReason = this.markingReasonControl.value;
      if (markingReason) {
        this.attendanceForm.patchValue({ markingReasonPublicId: markingReason }, { emitEvent: false });
      }
    });

    effect(() => {
      const error = this.attendanceStore.error();
      if (error) {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: error });
        this.attendanceStore.clearError();
      }
    });

    effect(() => {
      const response = this.lastMarkingResponse();
      if (response) {
        const markingReasonName = this.isExternalMarking()
          ? this.externalMarkingReasons().find(r => r.publicId === this.markingReasonControl.value)?.name
          : this.markingReasons().find(r => r.publicId === this.markingReasonControl.value)?.name;

        this.recentRecords.update(records => [
          {
            documentNumber: response.personDocumentNumber || this.attendanceForm.value.personDocumentNumber!,
            entryType: response.entryType,
            time: new Date().toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }),
            markingReason: markingReasonName || 'N/A',
            timestamp: Date.now()
          },
          ...records.slice(0, 19)
        ]);

        this.messageService.add({
          severity: 'success',
          summary: 'Éxito',
          detail: `Marcado registrado: ${response.entryType} - ${response.markedAt}`,
          life: 3000
        });

        this.attendanceForm.patchValue({
          personDocumentNumber: ''
        });

        this.attendanceStore.clearLastMarking();
      }
    });
  }

  ngOnInit(): void {}

  setMarkingType(isExternal: boolean): void {
    this.isExternalMarking.set(isExternal);
    this.markingReasonControl.setValue('');
  }

  setEntryType(isEntry: boolean): void {
    this.attendanceForm.patchValue({ entryType: isEntry });
  }

  onScanSuccess(scannedCode: string): void {
    const now = Date.now();
    if (now - this.lastScanTimestamp < this.SCAN_DEBOUNCE_MS) {
      console.log('Scan ignored due to debounce');
      return;
    }
    this.lastScanTimestamp = now;

    console.log('AttendanceComponent: QR Code scanned:', scannedCode);
    this.attendanceForm.patchValue({ personDocumentNumber: scannedCode });

    if (this.subsidiaryControl.value) {
      this.attendanceForm.patchValue({ subsidiaryPublicId: this.subsidiaryControl.value });
    }
    if (this.markingReasonControl.value) {
      this.attendanceForm.patchValue({ markingReasonPublicId: this.markingReasonControl.value });
    }

    if (this.subsidiaryControl.valid && this.markingReasonControl.valid) {
      this.markAttendance();
    } else {
      this.messageService.add({
        severity: 'info',
        summary: 'Código Escaneado',
        detail: `Documento: ${scannedCode}. Complete la configuración para registrar.`,
        life: 2000
      });
    }
  }

  markAttendance(): void {
    this.attendanceForm.patchValue({
      subsidiaryPublicId: this.subsidiaryControl.value || '',
      markingReasonPublicId: this.markingReasonControl.value || ''
    });

    if (this.attendanceForm.invalid) {
      this.attendanceForm.markAllAsTouched();
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulario Incompleto',
        detail: 'Por favor complete todos los campos requeridos'
      });
      return;
    }

    const { personDocumentNumber, subsidiaryPublicId, markingReasonPublicId, entryType } = this.attendanceForm.value;

    if (this.isExternalMarking()) {
      this.attendanceStore.markExternal({
        personDocumentNumber: personDocumentNumber!,
        subsidiaryPublicId: subsidiaryPublicId!,
        markingReasonPublicId: markingReasonPublicId!,
        isEntry: entryType!
      }).subscribe();
    } else {
      this.attendanceStore.markEmployee({
        personDocumentNumber: personDocumentNumber!,
        subsidiaryPublicId: subsidiaryPublicId!,
        markingReasonPublicId: markingReasonPublicId!,
        isEntry: entryType!
      }).subscribe();
    }
  }
}