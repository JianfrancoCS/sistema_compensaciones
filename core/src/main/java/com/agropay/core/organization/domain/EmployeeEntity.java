package com.agropay.core.organization.domain;

import com.agropay.core.payroll.domain.ConceptEntity;
import com.agropay.core.states.domain.StateEntity;
import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tbl_employees", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity extends AbstractEntity<String> implements Persistable<String> {
    public static final String TABLE_NAME = "tbl_employees";

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "person_document_number", length = 15)
    private String personDocumentNumber;

    @Column(name = "code", updatable = false, nullable = false, unique = true)
    private UUID code;

    @Column(name = "custom_salary", precision = 10, scale = 2)
    private BigDecimal customSalary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retirement_concept_id")
    @ToString.Exclude
    private ConceptEntity retirementConcept;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_insurance_concept_id")
    @ToString.Exclude
    private ConceptEntity healthInsuranceConcept;

    // Campos de solo lectura para acceder al ID sin cargar la relación
    @Column(name = "retirement_concept_id", insertable = false, updatable = false)
    private Short retirementConceptId;

    @Column(name = "health_insurance_concept_id", insertable = false, updatable = false)
    private Short healthInsuranceConceptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsidiary_id", nullable = false)
    @ToString.Exclude
    private SubsidiaryEntity subsidiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    @ToString.Exclude
    private PositionEntity position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_document_number", referencedColumnName = "document_number", insertable = false, updatable = false)
    @ToString.Exclude
    private PersonEntity person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reports_to_employee_document_number", referencedColumnName = "person_document_number")
    @ToString.Exclude
    private EmployeeEntity manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    @ToString.Exclude
    private StateEntity state;

    @Column(name = "afp_affiliation_number", length = 50)
    private String afpAffiliationNumber;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "daily_basic_salary", precision = 10, scale = 2)
    private BigDecimal dailyBasicSalary;

    @PrePersist
    public void generateCode() {
        if (this.code == null) {
            this.code = UUID.randomUUID();
        }
    }

    @Override
    public String getId() {
        return this.personDocumentNumber;
    }

    @Override
    @Transient
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}
