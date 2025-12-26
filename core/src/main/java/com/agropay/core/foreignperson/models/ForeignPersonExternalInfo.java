package com.agropay.core.foreignperson.models;

import lombok.Builder;

@Builder
public record ForeignPersonExternalInfo(
    String documentNumber,
    String nombres,
    String paternalLastname,
    String maternalLastname,
    String birthDate,
    String nacionalidad,
    String calidadMigratoria,
    String fechaExpiracionResidencia,
    String fechaExpiracionCarnet,
    String fechaUltimaEmisionCarnet
) {}

