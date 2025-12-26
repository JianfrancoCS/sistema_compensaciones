package com.agropay.core.assignment.domain;

import com.agropay.core.shared.persistence.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;

@Entity
@Table(name = "tbl_tareo_employee_motives", schema = "app")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class TareoEmployeeMotiveEntity extends AbstractEntity<Long> {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tareo_employee_id", nullable = false)
    @ToString.Exclude
    private TareoEmployeeEntity tareoEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motive_id", nullable = false)
    @ToString.Exclude
    private TareoMotiveEntity motive;

    @Column(name = "applied_at")
    private LocalTime appliedAt;

    @Column(length = 500)
    private String observations;
}