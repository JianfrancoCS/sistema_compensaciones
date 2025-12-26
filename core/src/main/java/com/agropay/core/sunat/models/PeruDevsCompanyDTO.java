package com.agropay.core.sunat.models;

public record PeruDevsCompanyDTO(
        boolean estado,
        String mensaje,
        PeruDevsCompanyDetailDTO resultado
) {
    public static record PeruDevsCompanyDetailDTO  (
        String razon_social,
        String condicion,
        String nombre_comercial,
        String tipo,
        String estado,
        String id
    ){

    }
}
