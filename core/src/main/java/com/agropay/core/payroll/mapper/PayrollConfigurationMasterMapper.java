package com.agropay.core.payroll.mapper;

import com.agropay.core.payroll.domain.PayrollConfigurationEntity;
import com.agropay.core.payroll.model.masterconfig.CommandPayrollConfigurationResponse;
import com.agropay.core.payroll.model.masterconfig.PayrollConfigurationListDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayrollConfigurationMasterMapper {

    CommandPayrollConfigurationResponse toCommandResponse(PayrollConfigurationEntity entity);

    PayrollConfigurationListDTO toListDTO(PayrollConfigurationEntity entity);

}
