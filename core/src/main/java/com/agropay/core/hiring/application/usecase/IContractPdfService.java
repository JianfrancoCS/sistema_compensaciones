package com.agropay.core.hiring.application.usecase;

import com.agropay.core.hiring.domain.ContractEntity;

/**
 * Interfaz para el caso de uso de generación de PDFs de contratos
 * Sigue el principio de inversión de dependencias (SOLID - DIP)
 */
public interface IContractPdfService {
    
    /**
     * Genera el PDF del contrato con las firmas incluidas y lo guarda como archivo interno
     * 
     * @param contract Entidad del contrato con todas sus variables cargadas
     * @param htmlContent Contenido HTML del contrato con todas las variables ya reemplazadas
     * @param isSigned Indica si el contrato está firmado (true = CONTRACT_SIGNED, false = CONTRACT_INITIAL)
     */
    void generateAndSaveContractPdf(ContractEntity contract, String htmlContent, boolean isSigned);
    
    /**
     * Procesa las firmas en el contenido HTML, convirtiendo las URLs a imágenes base64 embebidas
     * Este método puede ser usado tanto para PDFs como para vista previa HTML
     * 
     * @param htmlContent Contenido HTML con variables ya reemplazadas (puede contener URLs de firmas)
     * @param contract Entidad del contrato con todas sus variables cargadas
     * @return HTML procesado con las firmas convertidas a imágenes base64 embebidas
     */
    String processSignatureImagesForHtml(String htmlContent, ContractEntity contract);
    
    /**
     * Procesa los placeholders de firmas ANTES de que se reemplacen las variables.
     * Reemplaza directamente {{FIRMA_EMPLEADOR}} y {{FIRMA_ENCARGADO}} con imágenes base64.
     * Esto evita tener que buscar las URLs después de que se reemplacen las variables.
     * 
     * @param templateContent Contenido del template con placeholders sin reemplazar
     * @param contract Entidad del contrato con todas sus variables cargadas
     * @return Template procesado con los placeholders de firmas reemplazados por imágenes base64
     */
    String processSignaturePlaceholdersBeforeMerge(String templateContent, ContractEntity contract);
}

