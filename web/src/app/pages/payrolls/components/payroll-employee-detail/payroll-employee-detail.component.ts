import { Component, inject, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { PayrollStore } from '@core/store/payroll.store';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-payroll-employee-detail',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    ProgressSpinnerModule,
    TableModule,
    ToastModule
  ],
  templateUrl: './payroll-employee-detail.component.html',
  styleUrl: './payroll-employee-detail.component.css'
})
export class PayrollEmployeeDetailComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  readonly payrollStore = inject(PayrollStore);

  payrollPublicId: string | null = null;
  employeePublicId: string | null = null;

  constructor() {
    effect(() => {
      const employees = this.payrollStore.employees();
      if (employees.length > 0 && this.payrollPublicId && this.employeePublicId) {
        const employee = employees.find(e => e.publicId === this.employeePublicId);
        if (employee && !this.payrollStore.selectedEmployee()) {
          this.payrollStore.getPayrollEmployeeDetail({
            publicId: this.payrollPublicId!,
            employeeDocumentNumber: employee.employeeDocumentNumber
          });
        }
      }
    });
  }

  ngOnInit(): void {
    this.payrollPublicId = this.activatedRoute.snapshot.paramMap.get('payrollPublicId');
    this.employeePublicId = this.activatedRoute.snapshot.paramMap.get('employeePublicId');
    
    if (this.payrollPublicId && this.employeePublicId) {
      const employees = this.payrollStore.employees();
      if (employees.length > 0) {
        const employee = employees.find(e => e.publicId === this.employeePublicId);
        if (employee && !this.payrollStore.selectedEmployee()) {
          this.payrollStore.getPayrollEmployeeDetail({
            publicId: this.payrollPublicId,
            employeeDocumentNumber: employee.employeeDocumentNumber
          });
        }
      } else {
        this.payrollStore.getPayrollEmployees({ publicId: this.payrollPublicId });
      }
    }
  }

  goBack(): void {
    if (this.payrollPublicId) {
      this.router.navigate(['/system/payrolls', this.payrollPublicId, 'summary']);
    } else {
      this.router.navigate(['/system/payrolls']);
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN'
    }).format(amount);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-PE', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric' 
    });
  }

  getDayName(dayOfWeek: string): string {
    const days: Record<string, string> = {
      'MONDAY': 'Lunes',
      'TUESDAY': 'Martes',
      'WEDNESDAY': 'Miércoles',
      'THURSDAY': 'Jueves',
      'FRIDAY': 'Viernes',
      'SATURDAY': 'Sábado',
      'SUNDAY': 'Domingo'
    };
    return days[dayOfWeek] || dayOfWeek;
  }

  getConceptDisplayName(conceptCode: string): string {
    const conceptNames: Record<string, string> = {
      'BASIC_SALARY': 'Sueldo Básico',
      'OVERTIME': 'Horas Extras',
      'ATTENDANCE_BONUS': 'Bono por Asistencia',
      'PRODUCTIVITY_BONUS': 'Bono por Productividad',
      'FAMILY_ALLOWANCE': 'Asignación Familiar',
      'AFP_INTEGRA': 'AFP Integra',
      'AFP_PRIMA': 'AFP Prima',
      'AFP_PROFUTURO': 'AFP Profuturo',
      'AFP_HABITAT': 'AFP Habitat',
      'ONP': 'ONP',
      'ESSALUD': 'ESSALUD',
      'SEGURO_VIDA_LEY': 'Seguro de Vida Ley'
    };
    return conceptNames[conceptCode] || conceptCode;
  }

  getConceptsByCategory(category: string): Array<{code: string, name: string, amount: number}> {
    const emp = this.payrollStore.selectedEmployee();
    if (!emp || !emp.calculatedConcepts) {
      return [];
    }

    return Object.entries(emp.calculatedConcepts)
      .filter(([_, data]: [string, any]) => data?.category === category && data?.amount > 0)
      .map(([code, data]: [string, any]) => ({
        code,
        name: this.getConceptDisplayName(code),
        amount: data.amount || 0
      }))
      .sort((a, b) => b.amount - a.amount);
  }

  getAllDeductionConcepts(): Array<{code: string, name: string, amount: number}> {
    const retirement = this.getConceptsByCategory('RETIREMENT');
    const deduction = this.getConceptsByCategory('DEDUCTION');
    const employeeContribution = this.getConceptsByCategory('EMPLOYEE_CONTRIBUTION');
    
    return [...retirement, ...deduction, ...employeeContribution]
      .sort((a, b) => b.amount - a.amount);
  }
}

