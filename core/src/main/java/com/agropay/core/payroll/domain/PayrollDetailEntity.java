package com.agropay.core.payroll.domain;

import com.agropay.core.files.application.usecase.IFileable;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = PayrollDetailEntity.TABLE_NAME, schema = "app",
    indexes = {
        @Index(name = "IX_payroll_details_employee", columnList = "employee_document_number")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_payroll_details_public_id", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_payroll_details_payroll_employee", columnNames = {"payroll_id", "employee_document_number"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollDetailEntity extends AbstractEntity<Long> implements IFileable {

    public static final String TABLE_NAME = "tbl_payroll_details";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false)
    private PayrollEntity payroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_document_number", referencedColumnName = "person_document_number", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "calculated_concepts", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String calculatedConcepts;

    @Column(name = "daily_detail", columnDefinition = "NVARCHAR(MAX)")
    private String dailyDetail;

    @Column(name = "total_income", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_employer_contributions", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalEmployerContributions = BigDecimal.ZERO;

    @Column(name = "net_to_pay", precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal netToPay = BigDecimal.ZERO;

    @Column(name = "days_worked")
    private Short daysWorked;

    @Column(name = "normal_hours", precision = 8, scale = 2)
    private BigDecimal normalHours;

    @Column(name = "overtime_hours_25", precision = 8, scale = 2)
    private BigDecimal overtimeHours25;

    @Column(name = "overtime_hours_100", precision = 8, scale = 2)
    private BigDecimal overtimeHours100;

    @Column(name = "overtime_hours_35", precision = 8, scale = 2)
    private BigDecimal overtimeHours35;

    @Column(name = "night_hours", precision = 8, scale = 2)
    private BigDecimal nightHours;

    @Column(name = "total_hours", precision = 8, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "payslip_pdf_url", length = 500)
    private String payslipPdfUrl;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

    @Override
    public String getSimpleName() {
        return TABLE_NAME;
    }

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }
}