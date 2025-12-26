package com.agropay.core.attendance.persistence;

import com.agropay.core.attendance.domain.MarkingDetailEntity;
import com.agropay.core.attendance.domain.MarkingEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for building dynamic queries for attendance data.
 * Provides flexible filtering options for attendance reports and listings.
 */
public class AttendanceSpecification {

    /**
     * Build a composite specification for attendance filtering
     * @param markingDate Required date filter
     * @param subsidiaryPublicId Optional subsidiary filter
     * @param personDocumentNumber Optional person document filter
     * @param isEmployee Optional employee vs external person filter
     * @return Composite specification for the query
     */
    public static Specification<MarkingDetailEntity> buildSpecification(
            LocalDate markingDate,
            UUID subsidiaryPublicId,
            String personDocumentNumber,
            Boolean isEmployee
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join with marking entity to access subsidiary and date
            Join<MarkingDetailEntity, MarkingEntity> markingJoin = root.join("marking");

            // Required: Filter by marking date
            predicates.add(criteriaBuilder.equal(markingJoin.get("markingDate"), markingDate));

            // Optional: Filter by subsidiary
            if (subsidiaryPublicId != null) {
                Join<MarkingEntity, SubsidiaryEntity> subsidiaryJoin = markingJoin.join("subsidiary");
                predicates.add(criteriaBuilder.equal(subsidiaryJoin.get("publicId"), subsidiaryPublicId));
            }

            // Optional: Filter by person document number
            if (personDocumentNumber != null && !personDocumentNumber.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("personDocumentNumber")),
                        "%" + personDocumentNumber.toLowerCase() + "%"
                ));
            }

            // Optional: Filter by employee vs external person
            if (isEmployee != null) {
                predicates.add(criteriaBuilder.equal(root.get("isEmployee"), isEmployee));
            }

            // Order by marked time descending (latest first)
            query.orderBy(criteriaBuilder.desc(root.get("markedAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to filter by date only
     * @param markingDate The date to filter by
     * @return Specification for date filtering
     */
    public static Specification<MarkingDetailEntity> byDate(LocalDate markingDate) {
        return (root, query, criteriaBuilder) -> {
            Join<MarkingDetailEntity, MarkingEntity> markingJoin = root.join("marking");
            return criteriaBuilder.equal(markingJoin.get("markingDate"), markingDate);
        };
    }

    /**
     * Specification to filter by subsidiary
     * @param subsidiaryPublicId The subsidiary public ID to filter by
     * @return Specification for subsidiary filtering
     */
    public static Specification<MarkingDetailEntity> bySubsidiary(UUID subsidiaryPublicId) {
        return (root, query, criteriaBuilder) -> {
            Join<MarkingDetailEntity, MarkingEntity> markingJoin = root.join("marking");
            Join<MarkingEntity, SubsidiaryEntity> subsidiaryJoin = markingJoin.join("subsidiary");
            return criteriaBuilder.equal(subsidiaryJoin.get("publicId"), subsidiaryPublicId);
        };
    }

    /**
     * Specification to filter by person document number
     * @param personDocumentNumber The document number to filter by
     * @return Specification for person filtering
     */
    public static Specification<MarkingDetailEntity> byPersonDocument(String personDocumentNumber) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("personDocumentNumber")),
                "%" + personDocumentNumber.toLowerCase() + "%"
        );
    }

    /**
     * Specification to filter by employee vs external person
     * @param isEmployee True for employees, false for external persons
     * @return Specification for employee type filtering
     */
    public static Specification<MarkingDetailEntity> byEmployeeType(boolean isEmployee) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isEmployee"), isEmployee);
    }

    /**
     * Specification for employees only
     * @return Specification that filters for employees only
     */
    public static Specification<MarkingDetailEntity> employeesOnly() {
        return byEmployeeType(true);
    }

    /**
     * Specification for external persons only
     * @return Specification that filters for external persons only
     */
    public static Specification<MarkingDetailEntity> externalPersonsOnly() {
        return byEmployeeType(false);
    }
}