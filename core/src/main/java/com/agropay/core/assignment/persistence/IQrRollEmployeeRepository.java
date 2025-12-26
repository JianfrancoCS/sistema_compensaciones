package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.QrRollEmployeeEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IQrRollEmployeeRepository extends ISoftRepository<QrRollEmployeeEntity, Long>,
        IFindByPublicIdRepository<QrRollEmployeeEntity> {

    @Query("SELECT CASE WHEN COUNT(qre) > 0 THEN true ELSE false END FROM QrRollEmployeeEntity qre " +
           "WHERE qre.qrRoll.id = :rollId " +
           "AND qre.employee.code = :employeeCode " +
           "AND qre.assignedDate = :date " +
           "AND qre.deletedAt IS NULL")
    boolean existsByRollIdAndEmployeeCodeAndDate(@Param("rollId") Integer rollId,
                                                   @Param("employeeCode") UUID employeeCode,
                                                   @Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(qre) > 0 THEN true ELSE false END FROM QrRollEmployeeEntity qre " +
           "WHERE qre.employee.code = :employeeCode " +
           "AND qre.assignedDate = :date " +
           "AND qre.deletedAt IS NULL")
    boolean existsByEmployeeCodeAndDate(@Param("employeeCode") UUID employeeCode, @Param("date") LocalDate date);

    @Query("SELECT qre FROM QrRollEmployeeEntity qre " +
           "WHERE qre.employee.code = :employeeCode " +
           "AND qre.assignedDate = :date " +
           "AND qre.deletedAt IS NULL")
    Optional<QrRollEmployeeEntity> findByEmployeeCodeAndDate(@Param("employeeCode") UUID employeeCode,
                                                               @Param("date") LocalDate date);

    @Query("SELECT qre FROM QrRollEmployeeEntity qre " +
           "WHERE qre.qrRoll.id = :rollId " +
           "AND qre.assignedDate = :date " +
           "AND qre.deletedAt IS NULL")
    Optional<QrRollEmployeeEntity> findByRollAndDate(@Param("rollId") Integer rollId,
                                                      @Param("date") LocalDate date);

    /**
     * Busca todos los QR Roll assignments de un empleado en un período de fechas
     */
    @Query("SELECT qre FROM QrRollEmployeeEntity qre " +
           "WHERE qre.employee.personDocumentNumber = :employeeDocumentNumber " +
           "AND qre.assignedDate BETWEEN :periodStart AND :periodEnd " +
           "AND qre.deletedAt IS NULL " +
           "ORDER BY qre.assignedDate ASC")
    java.util.List<QrRollEmployeeEntity> findByEmployeeAndPeriod(
        @Param("employeeDocumentNumber") String employeeDocumentNumber,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd
    );
}
