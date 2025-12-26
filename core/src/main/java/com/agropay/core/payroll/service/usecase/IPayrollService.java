package com.agropay.core.payroll.service.usecase;

import com.agropay.core.payroll.model.payroll.CommandPayrollResponse;
import com.agropay.core.payroll.model.payroll.CreatePayrollRequest;
import com.agropay.core.payroll.model.payroll.PayrollListDTO;
import com.agropay.core.payroll.model.payroll.PayrollPageableRequest;
import com.agropay.core.payroll.model.payroll.PayrollSummaryDTO;
import com.agropay.core.shared.utils.PagedResult;

import java.util.List;
import java.util.UUID;

public interface IPayrollService {

    /**
     * Creates a new Payroll instance based on a subsidiary and a payroll period.
     *
     * @param request The request containing the subsidiary and period public IDs.
     * @return A command response with the details of the newly created payroll.
     */
    CommandPayrollResponse createPayroll(CreatePayrollRequest request);

    /**
     * Launches the calculation process for a specific payroll.
     *
     * @param payrollPublicId The public ID of the payroll to launch.
     * @return A command response with the updated details of the payroll (e.g., new state).
     */
    CommandPayrollResponse launchPayrollCalculation(UUID payrollPublicId);

    /**
     * Retrieves a paginated and filtered list of payrolls.
     *
     * @param request The request containing pagination and filtering criteria.
     * @return A paged result of payroll list DTOs.
     */
    PagedResult<PayrollListDTO> listPayrolls(PayrollPageableRequest request);

    /**
     * Soft-deletes a payroll. Only payrolls in DRAFT state can be deleted.
     * If the payroll has already generated employee details (batch has been run),
     * it cannot be deleted.
     *
     * @param payrollPublicId The public ID of the payroll to delete.
     * @throws BusinessValidationException if the payroll is not in DRAFT state or has generated details.
     */
    void deletePayroll(UUID payrollPublicId);

    /**
     * Obtiene un resumen detallado de la planilla con totales y desglose por concepto.
     *
     * @param payrollPublicId El ID público de la planilla.
     * @return Un DTO con el resumen de la planilla incluyendo totales y desglose por concepto.
     * @throws BusinessValidationException si la planilla no es encontrada.
     */
    PayrollSummaryDTO getPayrollSummary(UUID payrollPublicId);

    /**
     * Genera las boletas de pago (PDFs) para todos los empleados de una planilla calculada.
     * Solo se puede ejecutar si la planilla está en estado CALCULATED y no tiene boletas generadas.
     *
     * @param payrollPublicId El ID público de la planilla.
     * @return Una respuesta con los detalles actualizados de la planilla.
     * @throws BusinessValidationException si la planilla no está en estado válido o ya tiene boletas generadas.
     */
    CommandPayrollResponse generatePayslips(UUID payrollPublicId);

    /**
     * Anula una planilla. Solo se puede anular si está en estado CALCULATED o APPROVED
     * y no tiene boletas generadas.
     *
     * @param payrollPublicId El ID público de la planilla a anular.
     * @return Una respuesta con los detalles actualizados de la planilla.
     * @throws BusinessValidationException si la planilla no está en estado válido o ya tiene boletas generadas.
     */
    CommandPayrollResponse cancelPayroll(UUID payrollPublicId);

    /**
     * Obtiene la lista de tareos procesados en una planilla específica.
     *
     * @param payrollPublicId El ID público de la planilla.
     * @return Lista de tareos procesados en la planilla.
     * @throws BusinessValidationException si la planilla no es encontrada.
     */
    List<com.agropay.core.assignment.model.tareo.TareoListDTO> getProcessedTareos(UUID payrollPublicId);

    /**
     * Obtiene la lista de empleados en una planilla con filtros opcionales por labor y DNI.
     *
     * @param payrollPublicId El ID público de la planilla.
     * @param laborPublicId ID público de la labor para filtrar (opcional).
     * @param employeeDocumentNumber DNI del empleado para filtrar (opcional, búsqueda parcial).
     * @return Lista de empleados con información resumida de su planilla.
     * @throws BusinessValidationException si la planilla no es encontrada.
     */
    List<com.agropay.core.payroll.model.payroll.PayrollEmployeeListDTO> getPayrollEmployees(
        UUID payrollPublicId,
        UUID laborPublicId,
        String employeeDocumentNumber
    );

    /**
     * Obtiene el detalle completo de un empleado en una planilla específica.
     *
     * @param payrollPublicId El ID público de la planilla.
     * @param employeeDocumentNumber DNI del empleado.
     * @return Detalle completo del empleado incluyendo conceptos calculados y días laborados.
     * @throws BusinessValidationException si la planilla o el empleado no son encontrados.
     */
    com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO getPayrollEmployeeDetail(
        UUID payrollPublicId,
        String employeeDocumentNumber
    );

}
