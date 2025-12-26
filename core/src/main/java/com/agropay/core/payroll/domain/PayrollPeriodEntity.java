package com.agropay.core.payroll.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tbl_payroll_periods", schema = "app",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_payroll_periods_public_id_active", columnNames = {"public_id"}),
        @UniqueConstraint(name = "UQ_payroll_periods_year_month_number_active", columnNames = {"year", "month", "period_number"})
    }
)
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPeriodEntity extends AbstractEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false)
    private Short year;

    @Column(nullable = false)
    private Byte month;

    @Column(name = "period_number", nullable = false)
    @Builder.Default
    private Byte periodNumber = 1; // Número de período dentro del mes (1, 2, 3, 4...)

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "declaration_date", nullable = false)
    private LocalDate declarationDate;
}
