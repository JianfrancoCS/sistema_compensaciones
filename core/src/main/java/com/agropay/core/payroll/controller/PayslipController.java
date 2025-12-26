package com.agropay.core.payroll.controller;

import com.agropay.core.files.application.usecase.IInternalFileStorageUseCase;
import com.agropay.core.payroll.model.payslip.PayslipListDTO;
import com.agropay.core.payroll.model.payslip.PayslipPageableRequest;
import com.agropay.core.payroll.service.PayslipPdfService;
import com.agropay.core.payroll.service.PayslipService;
import com.agropay.core.shared.utils.ApiResult;
import com.agropay.core.shared.utils.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payslips")
@RequiredArgsConstructor
@Tag(name = "Boletas de Pago", description = "Endpoints para listar y visualizar boletas de pago de empleados.")
@SecurityRequirement(name = "bearerAuth")
public class PayslipController {

    private final PayslipService payslipService;
    private final PayslipPdfService payslipPdfService;
    private final IInternalFileStorageUseCase internalFileStorageService;

    @Operation(
        summary = "Listar boletas con filtros y paginación",
        description = "Obtiene un listado paginado de las boletas de pago. " +
            "Si el usuario tiene un empleado asociado, solo muestra sus propias boletas. " +
            "Si el usuario es admin (sin empleado asociado), muestra todas las boletas o un mensaje indicando que no hay boletas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente."),
        @ApiResponse(responseCode = "400", description = "Parámetro de ordenamiento inválido.")
    })
    @GetMapping
    public ResponseEntity<ApiResult<PagedResult<PayslipListDTO>>> listPayslips(
        @Parameter(description = "Criterios de paginación y filtrado para las boletas.")
        @Valid PayslipPageableRequest request
    ) {
        PagedResult<PayslipListDTO> result = payslipService.listPayslips(request);
        return ResponseEntity.ok(ApiResult.success(result));
    }

    @Operation(
        summary = "Obtener PDF de boleta de pago",
        description = "Obtiene el PDF de una boleta de pago específica. " +
            "Si el usuario tiene un empleado asociado, solo puede ver sus propias boletas. " +
            "Si el usuario es admin, puede ver cualquier boleta."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF obtenido exitosamente."),
        @ApiResponse(responseCode = "404", description = "La boleta no fue encontrada o el usuario no tiene permisos para verla.")
    })
    @GetMapping("/{payslipPublicId}/pdf")
    public ResponseEntity<?> getPayslipPdf(
        @Parameter(description = "Identificador UUID de la boleta (PayrollDetail).", required = true)
        @PathVariable UUID payslipPublicId
    ) {
        // Validar permisos
        var detail = payslipService.getPayslipDetail(payslipPublicId);
        
        // Obtener la entidad completa para verificar si ya existe un PDF
        var payrollDetail = payslipService.getPayrollDetailEntity(payslipPublicId);
        
        // Si ya existe un PDF almacenado en la BD, obtenerlo directamente desde archivos internos
        if (payrollDetail.getPayslipPdfUrl() != null && !payrollDetail.getPayslipPdfUrl().isEmpty()) {
            try {
                // Extraer el publicId de la URL: /v1/internal-files/{publicId}/download
                String pdfUrl = payrollDetail.getPayslipPdfUrl();
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("/v1/internal-files/([0-9a-fA-F-]+)/download");
                java.util.regex.Matcher matcher = pattern.matcher(pdfUrl);
                
                if (matcher.find()) {
                    UUID filePublicId = UUID.fromString(matcher.group(1));
                    
                    // Obtener el archivo directamente desde la BD
                    var internalFile = internalFileStorageService.getFile(filePublicId);
                    
                    if (internalFile != null && internalFile.getFileContent() != null) {
                        byte[] pdfBytes = internalFile.getFileContent();
                        
                        org.slf4j.LoggerFactory.getLogger(PayslipController.class)
                            .info("Sirviendo PDF almacenado desde BD. Tamaño: {} bytes, PublicId: {}", pdfBytes.length, filePublicId);
                        
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        // Usar ContentDisposition.inline() para mostrar en el navegador, no descargar
                        String fileName = String.format("boleta_%s_%s.pdf", detail.employeeDocumentNumber(), payslipPublicId.toString().substring(0, 8));
                        ContentDisposition contentDisposition = ContentDisposition.inline()
                            .filename(fileName)
                            .build();
                        headers.setContentDisposition(contentDisposition);
                        headers.setContentLength(pdfBytes.length);
                        headers.set("X-Frame-Options", "SAMEORIGIN");
                        
                        org.slf4j.LoggerFactory.getLogger(PayslipController.class)
                            .debug("Headers configurados: Content-Type={}, Content-Disposition={}, X-Frame-Options={}", 
                                MediaType.APPLICATION_PDF, contentDisposition, "SAMEORIGIN");
                        
                        return ResponseEntity.ok()
                            .headers(headers)
                            .body(pdfBytes);
                    }
                }
            } catch (Exception e) {
                // Si falla al obtener el PDF desde la BD, generar uno nuevo
                org.slf4j.LoggerFactory.getLogger(PayslipController.class)
                    .warn("Error al obtener PDF desde BD: {}. Generando nuevo PDF.", e.getMessage());
            }
        }
        
        // Si no existe PDF almacenado, generar uno nuevo
        byte[] pdfBytes = payslipPdfService.generatePayslipPdf(
            detail.payrollPublicId(),
            detail.employeeDocumentNumber()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Usar ContentDisposition.inline() para mostrar en el navegador, no descargar
        ContentDisposition contentDisposition = ContentDisposition.inline()
            .filename(String.format("boleta_%s_%s.pdf", detail.employeeDocumentNumber(), payslipPublicId.toString().substring(0, 8)))
            .build();
        headers.setContentDisposition(contentDisposition);
        headers.setContentLength(pdfBytes.length);
        // Permitir que el PDF se muestre en iframes del mismo origen
        headers.set("X-Frame-Options", "SAMEORIGIN");
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
    }
}

