package com.agropay.core.sunat.models;

import lombok.Builder;

@Builder
public record CompanyExternalInfo(
    String ruc,
    String businessName,
    String tradeName,
    String status,
    String companyType
) {}