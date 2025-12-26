package com.agropay.core.files.application.usecase;

/**
 * Interfaz para entidades que pueden tener archivos asociados (polimorfismo)
 */
public interface IFileable {
    String getId(); // Cambiado a String para compatibilidad con IImageable
    String getSimpleName(); // Nombre simple de la clase (ej: "Contract", "Payslip")
}

