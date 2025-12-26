package com.agropay.core.assignment.model.tareo;

import java.util.List;
import java.util.UUID;

public record EmployeeSyncResponse(
        UUID publicId,
        String documentNumber,
        String names,
        String paternalLastname,
        String maternalLastname,
        String positionName,
        List<QrRollData> qrRolls
) {
    public record QrRollData(
            UUID qrRollEmployeeId,
            UUID qrRollId,
            Integer maxQrCodesPerDay,
            List<QrCodeData> qrCodes
    ) {}

    public record QrCodeData(
            UUID publicId,
            Boolean isUsed,
            Boolean isPrinted
    ) {}
}