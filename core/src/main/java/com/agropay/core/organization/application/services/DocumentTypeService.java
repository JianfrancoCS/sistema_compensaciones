package com.agropay.core.organization.application.services;

import com.agropay.core.organization.application.usecase.IDocumentTypeUseCase;
import com.agropay.core.organization.constant.DocumentTypeEnum;
import com.agropay.core.organization.domain.DocumentTypeEntity;
import com.agropay.core.organization.model.documenttype.DocumentTypeSelectOptionDTO;
import com.agropay.core.organization.persistence.IDocumentTypeRepository;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentTypeService implements IDocumentTypeUseCase {

    private final IDocumentTypeRepository documentTypeRepository;

    @Override
    public DocumentTypeEntity getDniDocumentType() {
        log.debug("Getting DNI document type");
        return documentTypeRepository.findByCode(DocumentTypeEnum.DNI.getCode())
                .orElseThrow(() -> new BusinessValidationException("exception.person.document-type.not-found", DocumentTypeEnum.DNI.getCode()));
    }

    @Override
    public DocumentTypeEntity getByPublicId(UUID publicId) {
        log.debug("Getting document type by public ID: {}", publicId);
        return documentTypeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IdentifierNotFoundException("exception.person.document-type.not-found", publicId.toString()));
    }

    @Override
    public DocumentTypeEntity getByCode(String code) {
        log.debug("Getting document type by code: {}", code);
        return documentTypeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessValidationException("exception.person.document-type.not-found", code));
    }

    @Override
    public void validateDocumentLength(String documentNumber, DocumentTypeEntity documentType) {
        log.debug("Validating document length for type: {} with length: {}", documentType.getCode(), documentType.getLength());
        if (documentNumber.length() != documentType.getLength()) {
            throw new BusinessValidationException("exception.person.document.invalid-length",
                    documentType.getLength().toString(), documentType.getName());
        }
    }

    @Override
    public void validateForeignDocumentType(DocumentTypeEntity documentType) {
        log.debug("Validating foreign document type: {}", documentType.getCode());
        if (!DocumentTypeEnum.CARNET_EXTRANJERIA.getCode().equals(documentType.getCode())) {
            throw new BusinessValidationException("exception.person.document-type.invalid");
        }
    }

    @Override
    public List<DocumentTypeSelectOptionDTO> getDocumentTypesForSelect() {
        log.debug("Getting all document types for selection");
        return documentTypeRepository.findAll().stream()
                .map(dt -> new DocumentTypeSelectOptionDTO(
                        dt.getPublicId(),
                        dt.getCode(),
                        dt.getName(),
                        dt.getLength()))
                .toList();
    }

    @Override
    public boolean isNationalDocumentType(DocumentTypeEntity documentType) {
        log.debug("Checking if document type {} is national", documentType.getCode());
        return DocumentTypeEnum.DNI.getCode().equals(documentType.getCode());
    }

    @Override
    public DocumentTypeEntity getForeignDocumentType() {
        log.debug("Getting foreign document type (CE)");
        return documentTypeRepository.findByCode(DocumentTypeEnum.CARNET_EXTRANJERIA.getCode())
                .orElseThrow(() -> new BusinessValidationException("exception.person.document-type.not-found", DocumentTypeEnum.CARNET_EXTRANJERIA.getCode()));
    }
}