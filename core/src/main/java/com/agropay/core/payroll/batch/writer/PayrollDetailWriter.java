package com.agropay.core.payroll.batch.writer;

import com.agropay.core.payroll.domain.PayrollDetailEntity;
import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.persistence.IPayrollDetailRepository;
import com.agropay.core.payroll.persistence.IPayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Writer que persiste los detalles de planilla calculados para cada empleado
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class PayrollDetailWriter implements ItemWriter<PayrollDetailEntity> {

    private final IPayrollDetailRepository payrollDetailRepository;
    private final IPayrollRepository payrollRepository;

    @Value("#{jobExecutionContext['payrollId']}")
    private Long payrollId;

    @Override
    public void write(Chunk<? extends PayrollDetailEntity> chunk) throws Exception {
        log.info("=== PayrollDetailWriter: Escribiendo {} detalles de planilla ===", chunk.size());

        if (chunk.isEmpty()) {
            log.warn("Chunk vacío recibido en PayrollDetailWriter");
            return;
        }

        // Cargar la entidad Payroll una sola vez
        PayrollEntity payroll = payrollRepository.findById(payrollId)
            .orElseThrow(() -> new IllegalStateException(
                "Planilla no encontrada con ID: " + payrollId
            ));

        log.info("Planilla encontrada: ID={}, Code={}", payroll.getId(), payroll.getCode());

        // Asignar payroll a cada detalle y loggear información
        for (PayrollDetailEntity detail : chunk.getItems()) {
            detail.setPayroll(payroll);
            log.debug("Detalle preparado para empleado: {}, Neto a pagar: {}", 
                detail.getEmployee().getPersonDocumentNumber(), 
                detail.getNetToPay());
        }

        // Guardar todos los detalles en batch
        java.util.List<PayrollDetailEntity> savedDetails = new java.util.ArrayList<>(chunk.getItems());
        savedDetails = payrollDetailRepository.saveAll(savedDetails);

        log.info("=== {} detalles de planilla guardados exitosamente ===", savedDetails.size());
        log.info("Empleados procesados en este chunk: {}", 
            savedDetails.stream()
                .map(d -> d.getEmployee().getPersonDocumentNumber())
                .collect(java.util.stream.Collectors.joining(", ")));
    }
}