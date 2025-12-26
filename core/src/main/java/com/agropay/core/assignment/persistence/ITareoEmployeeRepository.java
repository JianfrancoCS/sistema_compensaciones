package com.agropay.core.assignment.persistence;

import com.agropay.core.assignment.domain.TareoEmployeeEntity;
import com.agropay.core.shared.generic.persistence.IFindByPublicIdRepository;
import com.agropay.core.shared.generic.persistence.ISoftRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface ITareoEmployeeRepository extends ISoftRepository<TareoEmployeeEntity, Long>,
        IFindByPublicIdRepository<TareoEmployeeEntity> {

    @Query("SELECT te FROM TareoEmployeeEntity te WHERE te.tareo.id = :tareoId AND te.deletedAt IS NULL")
    java.util.List<TareoEmployeeEntity> findAllByTareoId(@Param("tareoId") Integer tareoId);

    @Query("SELECT CASE WHEN COUNT(te) > 0 THEN true ELSE false END FROM TareoEmployeeEntity te " +
           "JOIN te.tareo t " +
           "WHERE te.employee.code = :employeeCode " +
           "AND CAST(t.createdAt AS DATE) = :date " +
           "AND te.deletedAt IS NULL " +
           "AND t.deletedAt IS NULL")
    boolean existsByEmployeeCodeAndTareoDate(@Param("employeeCode") UUID employeeCode, @Param("date") LocalDate date);
    
    /**
     * Verifica si un empleado ya está en otra labor el mismo día
     * Un empleado solo puede estar en UNA labor por día
     * Maneja tanto tareos con lote como tareos administrativos sin lote
     */
    @Query("SELECT CASE WHEN COUNT(te) > 0 THEN true ELSE false END FROM TareoEmployeeEntity te " +
           "JOIN te.tareo t " +
           "WHERE te.employee.code = :employeeCode " +
           "AND CAST(t.createdAt AS DATE) = :date " +
           "AND t.labor.id != :currentLaborId " +
           "AND te.deletedAt IS NULL " +
           "AND t.deletedAt IS NULL")
    boolean existsByEmployeeCodeAndDateInDifferentLabor(
        @Param("employeeCode") UUID employeeCode, 
        @Param("date") LocalDate date,
        @Param("currentLaborId") Short currentLaborId
    );

    @Query("SELECT CASE WHEN COUNT(te) > 0 THEN true ELSE false END FROM TareoEmployeeEntity te " +
           "WHERE te.employee.code = :employeeCode " +
           "AND te.tareo.id = :tareoId " +
           "AND te.deletedAt IS NULL")
    boolean existsByEmployeeCodeAndTareoId(@Param("employeeCode") UUID employeeCode, @Param("tareoId") Integer tareoId);

    @Query("SELECT te FROM TareoEmployeeEntity te " +
           "WHERE te.tareo.id = :tareoId " +
           "AND te.employee.code = :employeeCode " +
           "AND te.deletedAt IS NULL")
    java.util.Optional<TareoEmployeeEntity> findByTareoIdAndEmployeeCode(@Param("tareoId") Integer tareoId, @Param("employeeCode") UUID employeeCode);

    @Query("SELECT te.employee.personDocumentNumber FROM TareoEmployeeEntity te WHERE te.tareo.id = :tareoId AND te.deletedAt IS NULL")
    java.util.List<String> findEmployeeDocumentNumbersByTareoId(@Param("tareoId") Integer tareoId);

    @Modifying
    @Query("UPDATE TareoEmployeeEntity te SET te.deletedAt = CURRENT_TIMESTAMP WHERE te.tareo.id = :tareoId AND te.employee.personDocumentNumber IN :documentNumbers AND te.deletedAt IS NULL")
    void softDeleteByTareoIdAndDocumentNumbers(@Param("tareoId") Integer tareoId, @Param("documentNumbers") java.util.List<String> documentNumbers);

    /**
     * Busca todos los tareos de un empleado en un período de fechas
     */
    @Query("SELECT te FROM TareoEmployeeEntity te " +
           "JOIN te.tareo t " +
           "WHERE te.employee.personDocumentNumber = :employeeDocumentNumber " +
           "AND CAST(t.createdAt AS date) BETWEEN :periodStart AND :periodEnd " +
           "AND te.deletedAt IS NULL " +
           "AND t.deletedAt IS NULL " +
           "ORDER BY t.createdAt ASC")
    java.util.List<TareoEmployeeEntity> findByEmployeeAndPeriod(
        @Param("employeeDocumentNumber") String employeeDocumentNumber,
        @Param("periodStart") LocalDate periodStart,
        @Param("periodEnd") LocalDate periodEnd
    );
}