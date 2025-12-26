package com.agropay.core.payroll.mapper;

import com.agropay.core.payroll.domain.PayrollPeriodEntity;
import com.agropay.core.payroll.model.period.CommandPayrollPeriodResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayrollPeriodMapper {

    CommandPayrollPeriodResponse toCommandResponse(PayrollPeriodEntity entity);

}
