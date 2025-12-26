package com.agropay.core.organization.model.person;

public record CommandPersonResponse(
    String documentNumber,
    String names,
    String paternalLastname,
    String maternalLastname,
    String gender,
    boolean isNational
) {}
