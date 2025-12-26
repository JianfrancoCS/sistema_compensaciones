package com.agropay.core.sunat.application;

import com.agropay.core.sunat.models.CompanyExternalInfo;

import java.util.Optional;

public interface ICompanyExternalProvider {
    Optional<CompanyExternalInfo> getCompany(String ruc);
}
