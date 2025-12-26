package com.agropay.core.payroll.controller;

import com.agropay.core.payroll.service.PayslipPdfService;
import com.agropay.core.payroll.service.usecase.IPayrollService;
import com.agropay.core.payroll.model.payroll.CommandPayrollResponse;
import com.agropay.core.payroll.model.payroll.CreatePayrollRequest;
import com.agropay.core.payroll.model.payroll.PayrollListDTO;
import com.agropay.core.payroll.model.payroll.PayrollPageableRequest;
import com.agropay.core.payroll.model.payroll.PayrollSummaryDTO;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payrolls")
@RequiredArgsConstructor
@Tag(name = "Gestión de Planillas (Nómina)", description = "Endpoints para crear, calcular, listar y gestionar las planillas de pago.")
public class PayrollController {

    private final IPayrollService payrollService;
    private final PayslipPdfService payslipPdfService;

    @Operation(
        summary = "Crear una nueva planilla",
        description = "Crea un registro de planilla asociando una sucursal y un período de pago. La planilla se crea en estado 'BORRADOR'."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Planilla creada exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o no se encontró una configuración de planilla activa."),
        @ApiResponse(responseCode = "404", description = "La sucursal o el período de pago especificado no fue encontrado."),
        @ApiResponse(responseCode = "409", description = "Conflicto de datos, por ejemplo, ya existe una planilla para la misma sucursal y período.")
    })
    @PostMapping
    public ResponseEntity<ApiResult<CommandPayrollResponse>> createPayroll(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos para crear la planilla (UUID de sucursal y período).")
        @RequestBody @Valid CreatePayrollRequest request
    ) {
        CommandPayrollResponse response = payrollService.createPayroll(request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Lanzar el cálculo de una planilla",
        description = "Inicia un proceso asíncrono (batch) que calcula todos los conceptos para cada empleado. Solo aplicable a planillas en estado 'BORRADOR'."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proceso de cálculo iniciado exitosamente."),
        @ApiResponse(responseCode = "400", description = "La planilla con el UUID especificado no fue encontrada."),
        @ApiResponse(responseCode = "409", description = "Conflicto de estado: la planilla no está en estado 'BORRADOR'.")
    })
    @PostMapping("/{publicId}/launch")
    public ResponseEntity<ApiResult<CommandPayrollResponse>> launchPayrollCalculation(
        @Parameter(description = "Identificador UUID de la planilla a calcular.", required = true)
        @PathVariable UUID publicId
    ) {
        CommandPayrollResponse response = payrollService.launchPayrollCalculation(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Listar planillas con filtros y paginación",
        description = "Obtiene un listado paginado de las planillas. Permite filtrar por sucursal, período y estado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente."),
        @ApiResponse(responseCode = "400", description = "Parámetro de ordenamiento inválido.")
    })
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<PayrollListDTO>>> listPayrolls(
        @Parameter(description = "Criterios de paginación y filtrado para las planillas.")
        @Valid PayrollPageableRequest request
    ) {
        PagedResult<PayrollListDTO> result = payrollService.listPayrolls(request);
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @Operation(
        summary = "Eliminar una planilla",
        description = "Realiza un borrado lógico de una planilla. Solo es posible si la planilla está en estado 'BORRADOR' y no tiene detalles de pago generados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Planilla eliminada exitosamente."),
        @ApiResponse(responseCode = "400", description = "La planilla con el UUID especificado no fue encontrada."),
        @ApiResponse(responseCode = "409", description = "Conflicto de estado: no se puede eliminar la planilla porque no está en estado 'BORRADOR' o porque ya tiene detalles calculados.")
    })
    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResult<Void>> deletePayroll(
        @Parameter(description = "Identificador UUID de la planilla a eliminar.", required = true)
        @PathVariable UUID publicId
    ) {
        payrollService.deletePayroll(publicId);
        return ResponseEntity.ok(ApiResult.success(null));
    }

    @Operation(
        summary = "Generar PDF de boleta de pago",
        description = "Genera el PDF de la boleta de pago para un empleado específico en una planilla. El formato sigue el estándar peruano de boletas de remuneraciones."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente."),
        @ApiResponse(responseCode = "404", description = "La planilla o el detalle del empleado no fue encontrado.")
    })
    @GetMapping("/{payrollPublicId}/payslip/{employeeDocumentNumber}")
    public ResponseEntity<byte[]> generatePayslipPdf(
        @Parameter(description = "Identificador UUID de la planilla.", required = true)
        @PathVariable UUID payrollPublicId,
        @Parameter(description = "Número de documento del empleado.", required = true)
        @PathVariable String employeeDocumentNumber
    ) {
        byte[] pdfBytes = payslipPdfService.generatePayslipPdf(payrollPublicId, employeeDocumentNumber);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
            String.format("boleta_%s_%s.pdf", employeeDocumentNumber, payrollPublicId.toString().substring(0, 8)));
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }

    @Operation(
        summary = "Generar PDF de ejemplo de boleta de pago",
        description = "Genera un PDF de ejemplo con datos mock para visualizar el formato de la boleta. Útil para pruebas y verificación del diseño."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF de ejemplo generado exitosamente.")
    })
    @GetMapping("/example/payslip")
    public ResponseEntity<byte[]> generateExamplePayslipPdf() {
        byte[] pdfBytes = payslipPdfService.generateExamplePayslipPdf();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "boleta_ejemplo.pdf");
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }

    @Operation(
        summary = "Obtener resumen de planilla",
        description = "Obtiene un resumen detallado de la planilla incluyendo totales, cantidad de empleados procesados, y desglose por concepto (remuneraciones, descuentos, aportaciones del empleador)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente."),
        @ApiResponse(responseCode = "400", description = "La planilla con el UUID especificado no fue encontrada.")
    })
    @GetMapping("/{publicId}/summary")
    public ResponseEntity<ApiResult<PayrollSummaryDTO>> getPayrollSummary(
        @Parameter(description = "Identificador UUID de la planilla.", required = true)
        @PathVariable UUID publicId
    ) {
        PayrollSummaryDTO summary = payrollService.getPayrollSummary(publicId);
        return ResponseEntity.ok(ApiResult.success(summary));
    }

    @Operation(
        summary = "Generar boletas de pago",
        description = "Genera los PDFs de boletas de pago para todos los empleados de una planilla calculada. Solo se puede ejecutar si la planilla está en estado CALCULATED y no tiene boletas generadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Generación de boletas iniciada exitosamente."),
        @ApiResponse(responseCode = "400", description = "La planilla no está en estado válido o ya tiene boletas generadas."),
        @ApiResponse(responseCode = "404", description = "La planilla con el UUID especificado no fue encontrada.")
    })
    @PostMapping("/{publicId}/generate-payslips")
    public ResponseEntity<ApiResult<CommandPayrollResponse>> generatePayslips(
        @Parameter(description = "Identificador UUID de la planilla.", required = true)
        @PathVariable UUID publicId
    ) {
        CommandPayrollResponse response = payrollService.generatePayslips(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Anular una planilla",
        description = "Anula una planilla. Solo se puede anular si está en estado CALCULATED o APPROVED y no tiene boletas generadas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Planilla anulada exitosamente."),
        @ApiResponse(responseCode = "400", description = "La planilla no está en estado válido o ya tiene boletas generadas."),
        @ApiResponse(responseCode = "404", description = "La planilla con el UUID especificado no fue encontrada.")
    })
    @PostMapping("/{publicId}/cancel")
    public ResponseEntity<ApiResult<CommandPayrollResponse>> cancelPayroll(
        @Parameter(description = "Identificador UUID de la planilla a anular.", required = true)
        @PathVariable UUID publicId
    ) {
        CommandPayrollResponse response = payrollService.cancelPayroll(publicId);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @Operation(
        summary = "Obtener tareos procesados de una planilla",
        description = "Obtiene la lista de tareos que fueron procesados en una planilla específica."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tareos obtenida exitosamente."),
        @ApiResponse(responseCode = "404", description = "La planilla con el UUID especificado no fue encontrada.")
    })
    @GetMapping("/{publicId}/processed-tareos")
    public ResponseEntity<ApiResult<java.util.List<com.agropay.core.assignment.model.tareo.TareoListDTO>>> getProcessedTareos(
        @Parameter(description = "Identificador UUID de la planilla.", required = true)
        @PathVariable UUID publicId
    ) {
        java.util.List<com.agropay.core.assignment.model.tareo.TareoListDTO> tareos = payrollService.getProcessedTareos(publicId);
        return ResponseEntity.ok(ApiResult.success(tareos));
    }

    @Operation(
        summary = "Obtener lista de empleados de una planilla",
        description = "Obtiene la lista de empleados con información resumida de su planilla. Permite filtrar por labor y DNI."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de empleados obtenida exitosamente."),
        @ApiResponse(responseCode = "404", description = "La planilla con el UUID especificado no fue encontrada.")
    })
    @GetMapping("/{publicId}/employees")
    public ResponseEntity<ApiResult<java.util.List<com.agropay.core.payroll.model.payroll.PayrollEmployeeListDTO>>> getPayrollEmployees(
        @Parameter(description = "Identificador UUID de la planilla.", required = true)
        @PathVariable UUID publicId,
        @Parameter(description = "ID público de la labor para filtrar (opcional).")
        @RequestParam(required = false) UUID laborPublicId,
        @Parameter(description = "DNI del empleado para filtrar (opcional, búsqueda parcial).")
        @RequestParam(required = false) String employeeDocumentNumber
    ) {
        java.util.List<com.agropay.core.payroll.model.payroll.PayrollEmployeeListDTO> employees = 
            payrollService.getPayrollEmployees(publicId, laborPublicId, employeeDocumentNumber);
        return ResponseEntity.ok(ApiResult.success(employees));
    }

    @Operation(
        summary = "Obtener detalle de un empleado en una planilla",
        description = "Obtiene el detalle completo de un empleado en una planilla específica, incluyendo conceptos calculados y días laborados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle del empleado obtenido exitosamente."),
        @ApiResponse(responseCode = "404", description = "La planilla o el empleado no fueron encontrados.")
    })
    @GetMapping("/{publicId}/employees/{employeeDocumentNumber}")
    public ResponseEntity<ApiResult<com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO>> getPayrollEmployeeDetail(
        @Parameter(description = "Identificador UUID de la planilla.", required = true)
        @PathVariable UUID publicId,
        @Parameter(description = "DNI del empleado.", required = true)
        @PathVariable String employeeDocumentNumber
    ) {
        com.agropay.core.payroll.model.payroll.PayrollEmployeeDetailDTO detail = 
            payrollService.getPayrollEmployeeDetail(publicId, employeeDocumentNumber);
        return ResponseEntity.ok(ApiResult.success(detail));
    }
}
