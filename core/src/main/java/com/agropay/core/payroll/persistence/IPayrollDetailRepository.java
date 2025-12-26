package com.agropay.core.payroll.persistence;

import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.model.payroll.PayrollTotalsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IPayrollDetailRepository extends JpaRepository<PayrollDetailEntity, Long>, JpaSpecificationExecutor<PayrollDetailEntity> {

    Optional<PayrollDetailEntity> findByPublicId(UUID publicId);

    List<PayrollDetailEntity> findByPayrollId(Long payrollId);

    @Query("""
        SELECT pd FROM PayrollDetailEntity pd
        LEFT JOIN FETCH pd.employee e
        LEFT JOIN FETCH e.person
        LEFT JOIN FETCH e.position
        LEFT JOIN FETCH e.subsidiary
        LEFT JOIN FETCH e.retirementConcept
        LEFT JOIN FETCH e.healthInsuranceConcept
        WHERE pd.payroll.id = :payrollId AND e.personDocumentNumber = :employeeDocumentNumber
    """)
    Optional<PayrollDetailEntity> findByPayrollIdAndEmployeeDocumentNumber(
        @Param("payrollId") Long payrollId,
        @Param("employeeDocumentNumber") String employeeDocumentNumber
    );

    @Query("SELECT pd FROM PayrollDetailEntity pd WHERE pd.employee.personDocumentNumber = :employeeDocumentNumber ORDER BY pd.createdAt DESC")
    List<PayrollDetailEntity> findByEmployeePersonDocumentNumberOrderByCreatedAtDesc(@Param("employeeDocumentNumber") String employeeDocumentNumber);

    @Query("""
        SELECT new com.agropay.core.payroll.model.payroll.PayrollTotalsDTO(
            COUNT(pd.id),
            SUM(pd.totalIncome),
            SUM(pd.totalDeductions),
            SUM(pd.totalEmployerContributions)
        )
        FROM PayrollDetailEntity pd
        WHERE pd.payroll.id = :payrollId
    """)
    PayrollTotalsDTO getPayrollTotals(@Param("payrollId") Long payrollId);

    @Query("""
        SELECT COUNT(pd.id) > 0
        FROM PayrollDetailEntity pd
        WHERE pd.payroll.id = :payrollId
        AND pd.payslipPdfUrl IS NOT NULL
        AND pd.payslipPdfUrl != ''
    """)
    Boolean hasPayslips(@Param("payrollId") Long payrollId);

    /**
     * Verifica si un tareo (por fecha y empleado) está calculado en alguna planilla
     * Un tareo está calculado si existe un PayrollDetailEntity donde:
     * - El empleado del tareo esté en el detalle
     * - La fecha del tareo esté dentro del rango periodStart y periodEnd de la planilla
     * - La planilla tenga estado CALCULATED o superior (código >= 'CALCULATED')
     */
    @Query("""
        SELECT COUNT(pd.id) > 0
        FROM PayrollDetailEntity pd
        JOIN pd.payroll p
        JOIN p.state s
        WHERE pd.employee.personDocumentNumber = :employeeDocumentNumber
        AND CAST(:tareoDate AS date) BETWEEN p.periodStart AND p.periodEnd
        AND s.code IN ('CALCULATED', 'APPROVED', 'PAID')
    """)
    Boolean isTareoCalculated(
        @Param("employeeDocumentNumber") String employeeDocumentNumber,
        @Param("tareoDate") java.time.LocalDate tareoDate
    );

    /**
     * Verifica si un tareo (por ID) tiene al menos un empleado calculado en alguna planilla
     * Un tareo está calculado si al menos uno de sus empleados tiene un PayrollDetailEntity donde:
     * - La fecha del tareo esté dentro del rango periodStart y periodEnd de la planilla
     * - La planilla tenga estado CALCULATED o superior
     */
    @Query("""
        SELECT COUNT(pd.id) > 0
        FROM PayrollDetailEntity pd
        JOIN pd.payroll p
        JOIN p.state s
        WHERE EXISTS (
            SELECT 1
            FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te
            JOIN te.tareo t
            WHERE te.tareo.id = :tareoId
            AND te.employee.personDocumentNumber = pd.employee.personDocumentNumber
            AND CAST(t.createdAt AS date) BETWEEN p.periodStart AND p.periodEnd
            AND te.deletedAt IS NULL
            AND t.deletedAt IS NULL
        )
        AND s.code IN ('CALCULATED', 'APPROVED', 'PAID')
    """)
    Boolean isTareoIdCalculated(@Param("tareoId") Integer tareoId);

    /**
     * Cuenta los tareos únicos procesados en una planilla específica.
     * Un tareo está procesado si al menos uno de sus empleados tiene un PayrollDetailEntity
     * donde la fecha del tareo esté dentro del rango periodStart y periodEnd de la planilla.
     */
    @Query("""
        SELECT COUNT(DISTINCT t.id)
        FROM com.agropay.core.assignment.domain.TareoEntity t
        WHERE EXISTS (
            SELECT 1
            FROM PayrollDetailEntity pd
            JOIN pd.payroll p
            WHERE pd.employee.personDocumentNumber IN (
                SELECT te.employee.personDocumentNumber
                FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te
                WHERE te.tareo.id = t.id
                AND te.deletedAt IS NULL
            )
            AND p.id = :payrollId
            AND EXISTS (
                SELECT 1
                FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te2
                WHERE te2.tareo.id = t.id
                AND te2.employee.personDocumentNumber = pd.employee.personDocumentNumber
                AND CAST(t.createdAt AS date) BETWEEN p.periodStart AND p.periodEnd
                AND te2.deletedAt IS NULL
            )
        )
        AND t.deletedAt IS NULL
    """)
    Long countProcessedTareosByPayrollId(@Param("payrollId") Long payrollId);

    /**
     * Obtiene los tareos únicos procesados en una planilla específica.
     * Retorna los tareos que tienen al menos un empleado con PayrollDetailEntity
     * donde la fecha del tareo esté dentro del rango periodStart y periodEnd de la planilla.
     */
    @Query("""
        SELECT DISTINCT t
        FROM com.agropay.core.assignment.domain.TareoEntity t
        WHERE EXISTS (
            SELECT 1
            FROM PayrollDetailEntity pd
            JOIN pd.payroll p
            WHERE pd.employee.personDocumentNumber IN (
                SELECT te.employee.personDocumentNumber
                FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te
                WHERE te.tareo.id = t.id
                AND te.deletedAt IS NULL
            )
            AND p.id = :payrollId
            AND EXISTS (
                SELECT 1
                FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te2
                WHERE te2.tareo.id = t.id
                AND te2.employee.personDocumentNumber = pd.employee.personDocumentNumber
                AND CAST(t.createdAt AS date) BETWEEN p.periodStart AND p.periodEnd
                AND te2.deletedAt IS NULL
            )
        )
        AND t.deletedAt IS NULL
        ORDER BY t.createdAt DESC
    """)
    List<com.agropay.core.assignment.domain.TareoEntity> findProcessedTareosByPayrollId(@Param("payrollId") Long payrollId);

    /**
     * Cuenta los empleados procesados (con detalles) en una planilla específica.
     * Esto muestra el progreso en tiempo real del batch.
     */
    @Query("""
        SELECT COUNT(DISTINCT pd.id)
        FROM PayrollDetailEntity pd
        WHERE pd.payroll.id = :payrollId
    """)
    Long countProcessedEmployeesByPayrollId(@Param("payrollId") Long payrollId);

    /**
     * Obtiene los detalles de planilla filtrados por labor y/o DNI de empleado.
     * Filtra por labor si el empleado tiene tareos con esa labor dentro del período de la planilla.
     */
    @Query("""
        SELECT DISTINCT pd FROM PayrollDetailEntity pd
        LEFT JOIN FETCH pd.employee e
        LEFT JOIN FETCH e.person
        LEFT JOIN FETCH e.position
        WHERE pd.payroll.id = :payrollId
        AND (:laborPublicId IS NULL OR EXISTS (
            SELECT 1
            FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te
            JOIN te.tareo t
            JOIN t.labor l
            WHERE te.employee.personDocumentNumber = e.personDocumentNumber
            AND l.publicId = :laborPublicId
            AND CAST(t.createdAt AS date) BETWEEN :periodStart AND :periodEnd
            AND te.deletedAt IS NULL
            AND t.deletedAt IS NULL
        ))
        AND (:employeeDocumentNumber IS NULL OR e.personDocumentNumber LIKE :employeeDocumentNumber)
        ORDER BY e.personDocumentNumber ASC
    """)
    List<PayrollDetailEntity> findByPayrollIdWithFilters(
        @Param("payrollId") Long payrollId,
        @Param("laborPublicId") UUID laborPublicId,
        @Param("employeeDocumentNumber") String employeeDocumentNumber,
        @Param("periodStart") java.time.LocalDate periodStart,
        @Param("periodEnd") java.time.LocalDate periodEnd
    );

}
