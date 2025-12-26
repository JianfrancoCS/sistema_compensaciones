package com.agropay.core.organization.domain;

import com.agropay.core.address.application.IAddressableUseCase;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tbl_companies", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class CompanyEntity extends AbstractEntity<Long> implements IAddressableUseCase<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "public_id", updatable = false, nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false, name = "legal_name")
    private String legalName;

    @Column(nullable = false, name = "trade_name")
    private String tradeName;

    @Column(nullable = false, unique = true)
    private String ruc;

    @Column(nullable = false,name = "company_type")
    private String companyType;

    // Payroll Configuration
    @Column(name = "payroll_payment_interval")
    private Integer paymentIntervalDays;

    @Column(name = "payroll_declaration_day")
    private Byte payrollDeclarationDay;

    @Column(name = "payroll_anticipation_days")
    private Byte payrollAnticipationDays;

    @Column(name = "max_monthly_working_hours")
    private Integer maxMonthlyWorkingHours;

    @Column(name = "overtime_rate", precision = 5, scale = 2)
    private BigDecimal overtimeRate;

    @Column(name = "daily_normal_hours", precision = 4, scale = 2)
    private BigDecimal dailyNormalHours;

    @Column(name = "month_calculation_days")
    private Integer monthCalculationDays;

    @Column(name = "rmv", precision = 10, scale = 2)
    private BigDecimal rmv; // Remuneración Mínima Vital (RMV)

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getAddressableType() {
        return this.getClass().getSimpleName();
    }

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}
