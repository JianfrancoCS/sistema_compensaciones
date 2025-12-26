package com.agropay.core.attendance.application.usecase;

import com.agropay.core.attendance.model.marking.EmployeeMarkRequest;
import com.agropay.core.attendance.model.marking.ExternalMarkRequest;
import com.agropay.core.attendance.model.marking.MarkingResponse;

import java.time.LocalDate;

public interface IMarkingUseCase {

    MarkingResponse markEmployee(EmployeeMarkRequest request);

    MarkingResponse markExternal(ExternalMarkRequest request);

    boolean hasEmployeeMarkedEntryOnDate(String documentNumber, LocalDate date);
}