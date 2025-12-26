package com.agropay.core.payroll.domain;

import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import com.agropay.core.states.domain.StateEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tbl_payrolls", schema = "app",
    indexes = {
        @Index(name = "IX_payrolls_subsidiary_period", columnList = "subsidiary_id, year, month, deleted_at"),
        @Index(name = "IX_payrolls_state", columnList = "state_id, deleted_at"),
        @Index(name = "IX_payrolls_base", columnList = "base_payroll_id"),
        @Index(name = "IX_payrolls_corrected", columnList = "corrected_payroll_id"),
        @Index(name = "IX_payrolls_payroll_configuration", columnList = "payroll_configuration_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_payrolls_public_id_active", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_payrolls_code_active", columnNames = {"code"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollEntity extends AbstractEntity<Long> {

    public static final String TABLE_NAME = "tbl_payrolls";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsidiary_id", nullable = false)
    private SubsidiaryEntity subsidiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    private PayrollPeriodEntity period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_configuration_id", nullable = false)
    private PayrollConfigurationEntity payrollConfiguration;

    @Column(nullable = false)
    private Short year;

    @Column(nullable = false)
    private Short month;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "week_start")
    private Short weekStart;

    @Column(name = "week_end")
    private Short weekEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private StateEntity state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_payroll_id")
    private PayrollEntity basePayroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_payroll_id")
    private PayrollEntity correctedPayroll;

    @Column(name = "total_employees")
    @Builder.Default
    private Integer totalEmployees = 0;

    @Column(name = "total_income", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Column(name = "total_deductions", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_net", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @OneToMany(mappedBy = "payroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PayrollDetailEntity> details;

    @OneToMany(mappedBy = "payroll", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PayrollConceptAssignmentEntity> payrollConceptAssignments;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}
