package com.agropay.core.foreignperson.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VerificaIdResponseDTO(
    String type,
    Integer status,
    String message,
    VerificaIdDataDTO data
) {
    public record VerificaIdDataDTO(
        @JsonProperty("numero_de_documento")
        String numeroDeDocumento,
        @JsonProperty("calidad_migratoria")
        String calidadMigratoria,
        String nombres,
        String nacionalidad,
        @JsonProperty("apellido_paterno")
        String apellidoPaterno,
        @JsonProperty("apellido_materno")
        String apellidoMaterno,
        @JsonProperty("fecha_nacimiento")
        String fechaNacimiento,
        @JsonProperty("fecha_expiracion_residencia")
        String fechaExpiracionResidencia,
        @JsonProperty("fecha_expiracion_carnet")
        String fechaExpiracionCarnet,
        @JsonProperty("fecha_ultima_emision_carnet")
        String fechaUltimaEmisionCarnet
    ) {}
}

