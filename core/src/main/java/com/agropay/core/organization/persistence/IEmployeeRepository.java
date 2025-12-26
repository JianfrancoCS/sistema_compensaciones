package com.agropay.core.organization.persistence;

import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.PositionEntity;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IEmployeeRepository extends ISoftRepository<EmployeeEntity, String>, JpaSpecificationExecutor<EmployeeEntity> {
    boolean existsByPositionId(Short id);

    long countByPositionId(Short id);

    long countBySubsidiaryId(Short id);

    long countByManagerPersonDocumentNumber(String dni);

    @Query("SELECT e FROM EmployeeEntity e WHERE e.code = :code AND e.deletedAt IS NULL")
    Optional<EmployeeEntity> findByCode(UUID code);

    @Query("SELECT e FROM EmployeeEntity e WHERE e.subsidiary.publicId = :subsidiaryId AND e.deletedAt IS NULL")
    List<EmployeeEntity> findAllBySubsidiary_PublicId(UUID subsidiaryId);

    @Query("SELECT e FROM EmployeeEntity e WHERE e.deletedAt IS NULL")
    List<EmployeeEntity> findAllActive();

    // New method to find all employees holding a specific position
    List<EmployeeEntity> findAllByPosition(PositionEntity position);

    // Method to find employee by person document number (should be unique)
    Optional<EmployeeEntity> findByPersonDocumentNumber(String personDocumentNumber);

    // Method to check if there's already a CEO (employee without manager)
    @Query("SELECT COUNT(e) > 0 FROM EmployeeEntity e WHERE e.manager IS NULL AND e.deletedAt IS NULL")
    boolean existsCeoEmployee();

    // Dashboard query
    @Query("""
        SELECT s.name, COUNT(e)
        FROM EmployeeEntity e
        JOIN e.subsidiary s
        WHERE e.deletedAt IS NULL
        AND (:subsidiaryId IS NULL OR s.id = :subsidiaryId)
        GROUP BY s.id, s.name
        ORDER BY COUNT(e) DESC
    """)
    List<Object[]> getEmployeesBySubsidiaryGrouped(@Param("subsidiaryId") Short subsidiaryId);

}
