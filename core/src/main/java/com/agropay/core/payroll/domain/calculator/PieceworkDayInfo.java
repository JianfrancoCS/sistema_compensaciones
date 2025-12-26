package com.agropay.core.payroll.domain.calculator;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Información de labor de destajo por día
 * Contiene los datos necesarios para calcular el pago del excedente de destajo
 */
public record PieceworkDayInfo(
    LocalDate date,
    BigDecimal minTaskRequirement, // Mínimo requerido (ej: 25 jabas)
    BigDecimal basePrice, // Precio por unidad excedente (ej: 1.50 soles por jaba)
    Integer productivityCount // Cantidad real producida (ej: 30 jabas)
) {
    /**
     * Calcula el excedente (cantidad producida - mínimo requerido)
     * Retorna 0 si no hay excedente
     */
    public BigDecimal calculateExcess() {
        if (productivityCount == null || minTaskRequirement == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal excess = BigDecimal.valueOf(productivityCount).subtract(minTaskRequirement);
        return excess.compareTo(BigDecimal.ZERO) > 0 ? excess : BigDecimal.ZERO;
    }
    
    /**
     * Calcula el pago del excedente (excedente * basePrice)
     */
    public BigDecimal calculateExcessPayment() {
        BigDecimal excess = calculateExcess();
        if (excess.compareTo(BigDecimal.ZERO) <= 0 || basePrice == null) {
            return BigDecimal.ZERO;
        }
        return excess.multiply(basePrice);
    }
}

