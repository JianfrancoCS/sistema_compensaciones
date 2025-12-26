package com.agropay.core.reniec.models;

public record PeruDevsPersonDTO(
        boolean estado,
        String mensaje,
        PeruDevsPersonDetailDTO resultado
) {
    public static record PeruDevsPersonDetailDTO(
            String id,
            String nombres,
            String apellido_paterno,
            String apellido_materno,
            String nombre_completo,
            String genero,
            String fecha_nacimiento,
            String codigo_verificacion
    ) {}
}