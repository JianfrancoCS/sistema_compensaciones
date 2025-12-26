package com.agropay.core.organization.application.usecase;

import com.agropay.core.organization.domain.DocumentTypeEntity;
import com.agropay.core.organization.model.documenttype.DocumentTypeSelectOptionDTO;

import java.util.List;
import java.util.UUID;

public interface IDocumentTypeUseCase {

    /**
     * Obtiene el tipo de documento DNI por defecto
     */
    DocumentTypeEntity getDniDocumentType();

    /**
     * Busca un tipo de documento por su ID público
     */
    DocumentTypeEntity getByPublicId(UUID publicId);

    /**
     * Busca un tipo de documento por su código
     */
    DocumentTypeEntity getByCode(String code);

    /**
     * Valida la longitud del documento según su tipo
     */
    void validateDocumentLength(String documentNumber, DocumentTypeEntity documentType);

    /**
     * Valida que el tipo de documento sea válido para creación manual (solo extranjeros)
     */
    void validateForeignDocumentType(DocumentTypeEntity documentType);

    /**
     * Obtiene todos los tipos de documento para selección
     */
    List<DocumentTypeSelectOptionDTO> getDocumentTypesForSelect();

    /**
     * Determina si el tipo de documento corresponde a una persona nacional
     */
    boolean isNationalDocumentType(DocumentTypeEntity documentType);

    /**
     * Obtiene el tipo de documento de extranjería (CE)
     */
    DocumentTypeEntity getForeignDocumentType();
}