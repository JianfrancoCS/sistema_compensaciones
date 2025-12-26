package com.agropay.core.payroll.mapper;

import com.agropay.core.payroll.domain.PayrollEntity;
import com.agropay.core.payroll.model.payroll.CommandPayrollResponse;
import com.agropay.core.payroll.model.payroll.PayrollListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Month;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface PayrollMapper {

    @Mapping(source = "subsidiary.name", target = "subsidiaryName")
    @Mapping(target = "periodName", expression = "java(getPeriodName(entity.getYear(), entity.getMonth(), entity.getPeriod() != null ? entity.getPeriod().getPeriodNumber() : null))")
    @Mapping(source = "state.name", target = "stateName")
    @Mapping(target = "hasPayslips", constant = "false") // Se calculará en el servicio
    @Mapping(source = "totalEmployees", target = "totalEmployees")
    @Mapping(target = "processedTareos", constant = "0L") // Se calculará en el servicio
    PayrollListDTO toListDTO(PayrollEntity entity);

    @Mapping(source = "state.name", target = "stateName")
    CommandPayrollResponse toCommandResponse(PayrollEntity entity);

    default String getPeriodName(Short year, Short month, Byte periodNumber) {
        if (year == null || month == null) {
            return "";
        }
        String[] monthNames = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                               "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        if (month >= 1 && month <= 12) {
            String monthName = monthNames[month - 1] + " " + year;
            // Si hay múltiples períodos en el mismo mes (periodNumber > 1), agregar el número
            if (periodNumber != null && periodNumber > 1) {
                return monthName + "-" + periodNumber;
            }
            return monthName;
        }
        return month + "/" + year;
    }
}
