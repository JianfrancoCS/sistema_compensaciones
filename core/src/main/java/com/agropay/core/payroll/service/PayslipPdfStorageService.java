package com.agropay.core.payroll.service;

import com.agropay.core.files.application.usecase.IInternalFileStorageUseCase;
import com.agropay.core.files.constant.FileCategory;
import com.agropay.core.files.domain.InternalFileEntity;
import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.shared.utils.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para almacenar PDFs de boletas de pago en SQL Server (archivos internos)
 * 
 * Los PDFs se almacenan en la base de datos usando el sistema de archivos internos.
 * Esto proporciona:
 * - Transaccionalidad completa
 * - Backup automático con la BD
 * - Seguridad y privacidad mejoradas
 * - Auditoría completa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipPdfStorageService {

    private final IInternalFileStorageUseCase internalFileStorageService;

    /**
     * Guarda un PDF de boleta de pago en SQL Server usando archivos internos
     * 
     * @param pdfBytes Bytes del PDF generado
     * @param payrollDetail Entidad PayrollDetailEntity asociada a la boleta
     * @return URL de descarga del PDF (formato: /v1/internal-files/{publicId}/download)
     */
    @Transactional
    public String uploadPayslipPdf(byte[] pdfBytes, PayrollDetailEntity payrollDetail) {
        try {
            log.info("Guardando PDF de boleta en SQL Server. Planilla: {}, Empleado: {}", 
                payrollDetail.getPayroll().getPublicId(), 
                payrollDetail.getEmployee().getPersonDocumentNumber());

            // Nombre del archivo: boleta_{payrollCode}_{employeeDni}.pdf
            String fileName = String.format("boleta_%s_%s.pdf", 
                payrollDetail.getPayroll().getCode(),
                payrollDetail.getEmployee().getPersonDocumentNumber());

            // Guardar archivo usando el servicio de archivos internos
            InternalFileEntity savedFile = internalFileStorageService.saveFile(
                    payrollDetail,
                    pdfBytes,
                    fileName,
                    "application/pdf",
                    FileCategory.PAYSLIP.getCode(),
                    String.format("Boleta de pago - Planilla %s - Empleado %s", 
                        payrollDetail.getPayroll().getCode(),
                        payrollDetail.getEmployee().getPersonDocumentNumber())
            );

            // Generar URL de descarga
            String downloadUrl = "/v1/internal-files/" + savedFile.getPublicId() + "/download";
            log.info("PDF guardado exitosamente. PublicId: {}, URL: {}", savedFile.getPublicId(), downloadUrl);

            return downloadUrl;

        } catch (Exception e) {
            log.error("Error al guardar PDF de boleta. Planilla: {}, Empleado: {}", 
                payrollDetail.getPayroll().getPublicId(), 
                payrollDetail.getEmployee().getPersonDocumentNumber(), e);
            throw new RuntimeException("Error al guardar PDF de boleta: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un PDF de boleta (soft delete) usando archivos internos
     * 
     * @param payrollDetail Entidad PayrollDetailEntity asociada a la boleta
     */
    @Transactional
    public void deletePayslipPdf(PayrollDetailEntity payrollDetail) {
        try {
            log.info("Eliminando PDF de boleta. Planilla: {}, Empleado: {}", 
                payrollDetail.getPayroll().getPublicId(), 
                payrollDetail.getEmployee().getPersonDocumentNumber());

            // Obtener archivos internos asociados a esta boleta
            var files = internalFileStorageService.getFilesByFileableAndCategory(
                    payrollDetail, FileCategory.PAYSLIP.getCode());

            // Eliminar todos los archivos asociados (soft delete)
            String currentUser = SecurityContextUtils.getCurrentUsername();
            for (var file : files) {
                internalFileStorageService.deleteFile(file.getPublicId(), currentUser);
                log.debug("PDF eliminado (soft delete). PublicId: {}", file.getPublicId());
            }

            log.info("PDFs eliminados exitosamente");

        } catch (Exception e) {
            log.error("Error al eliminar PDF de boleta. Planilla: {}, Empleado: {}", 
                payrollDetail.getPayroll().getPublicId(), 
                payrollDetail.getEmployee().getPersonDocumentNumber(), e);
            // No lanzar excepción, solo loggear (no crítico)
        }
    }
}

