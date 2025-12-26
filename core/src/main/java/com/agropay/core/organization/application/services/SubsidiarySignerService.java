package com.agropay.core.organization.application.services;

import com.agropay.core.files.application.usecase.IInternalFileStorageUseCase;
import com.agropay.core.files.constant.FileCategory;
import com.agropay.core.files.domain.InternalFileEntity;
import com.agropay.core.organization.application.usecase.ICompanyUseCase;
import com.agropay.core.organization.application.usecase.IEmployeeUseCase;
import com.agropay.core.organization.domain.CompanyEntity;
import com.agropay.core.organization.domain.CompanySubsidiarySignerEntity;
import com.agropay.core.organization.domain.EmployeeEntity;
import com.agropay.core.organization.domain.SubsidiaryEntity;
import com.agropay.core.organization.model.signer.CreateSubsidiarySignerRequest;
import com.agropay.core.organization.model.signer.SubsidiarySignerDetailsDTO;
import com.agropay.core.organization.model.signer.SubsidiarySignerListDTO;
import com.agropay.core.organization.model.signer.UpdateSubsidiarySignerRequest;
import com.agropay.core.organization.persistence.ICompanySubsidiarySignerRepository;
import com.agropay.core.organization.persistence.ISubsidiaryRepository;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.utils.PagedResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubsidiarySignerService {

    private final ICompanySubsidiarySignerRepository signerRepository;
    private final ISubsidiaryRepository subsidiaryRepository;
    private final ICompanyUseCase companyUseCase;
    private final IEmployeeUseCase employeeUseCase;
    private final IInternalFileStorageUseCase fileStorageService;

    /**
     * Lista todas las subsidiarias con información de su responsable de firma actual (sin paginación - para compatibilidad)
     */
    @Transactional(readOnly = true)
    public List<SubsidiarySignerListDTO> listSubsidiariesWithSigners() {
        log.info("Fetching all subsidiaries with their signers");
        
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        List<SubsidiaryEntity> subsidiaries = subsidiaryRepository.findAll();
        
        return subsidiaries.stream()
            .map(subsidiary -> buildSubsidiarySignerDTO(subsidiary, company))
            .toList();
    }

    /**
     * Lista subsidiarias con información de su responsable de firma con paginación y filtrado
     */
    @Transactional(readOnly = true)
    public PagedResult<SubsidiarySignerListDTO> listSubsidiariesWithSignersPaged(
            String subsidiaryName, 
            String responsibleEmployeeName, 
            Pageable pageable) {
        log.info("Fetching paged subsidiaries with signers - subsidiaryName: {}, responsibleEmployeeName: {}, page: {}, size: {}", 
            subsidiaryName, responsibleEmployeeName, pageable.getPageNumber(), pageable.getPageSize());
        
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        
        // Crear especificación para filtrar subsidiarias
        Specification<SubsidiaryEntity> spec = (root, query, cb) -> {
            if (StringUtils.hasText(subsidiaryName)) {
                return cb.like(cb.lower(root.get("name")), "%" + subsidiaryName.toLowerCase() + "%");
            }
            return cb.conjunction();
        };
        
        Page<SubsidiaryEntity> subsidiaryPage = subsidiaryRepository.findAll(spec, pageable);
        
        // Mapear a DTOs con información de signers
        List<SubsidiarySignerListDTO> dtos = subsidiaryPage.getContent().stream()
            .map(subsidiary -> buildSubsidiarySignerDTO(subsidiary, company))
            .filter(dto -> {
                // Filtrar por nombre de responsable si se especifica
                if (StringUtils.hasText(responsibleEmployeeName)) {
                    if (dto.responsibleEmployeeName() == null) {
                        return false;
                    }
                    return dto.responsibleEmployeeName().toLowerCase()
                        .contains(responsibleEmployeeName.toLowerCase());
                }
                return true;
            })
            .toList();
        
        // Crear PagedResult manualmente ya que filtramos después
        return new PagedResult<>(
            dtos,
            subsidiaryPage.getTotalElements(), // Nota: esto puede no ser exacto después del filtro
            subsidiaryPage.getNumber(),
            subsidiaryPage.getTotalPages(),
            subsidiaryPage.isFirst(),
            subsidiaryPage.isLast(),
            subsidiaryPage.hasNext(),
            subsidiaryPage.hasPrevious()
        );
    }

    /**
     * Construye el DTO de subsidiaria con información de signer
     */
    private SubsidiarySignerListDTO buildSubsidiarySignerDTO(SubsidiaryEntity subsidiary, CompanyEntity company) {
        Optional<CompanySubsidiarySignerEntity> signerOpt = signerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiary.getId());
        
        if (signerOpt.isPresent()) {
            CompanySubsidiarySignerEntity signer = signerOpt.get();
            EmployeeEntity employee = signer.getResponsibleEmployee();
            String employeeName = String.format("%s %s, %s",
                employee.getPerson().getPaternalLastname(),
                employee.getPerson().getMaternalLastname(),
                employee.getPerson().getNames());
            
            return new SubsidiarySignerListDTO(
                subsidiary.getPublicId(),
                subsidiary.getName(),
                employee.getPersonDocumentNumber(),
                employeeName,
                signer.getResponsiblePosition(),
                getSignatureImageUrl(signer),
                true
            );
        } else {
            // Buscar a nivel de empresa
            Optional<CompanySubsidiarySignerEntity> companySignerOpt = signerRepository
                .findLatestByCompany(company.getId());
            
            if (companySignerOpt.isPresent()) {
                CompanySubsidiarySignerEntity signer = companySignerOpt.get();
                EmployeeEntity employee = signer.getResponsibleEmployee();
                String employeeName = String.format("%s %s, %s",
                    employee.getPerson().getPaternalLastname(),
                    employee.getPerson().getMaternalLastname(),
                    employee.getPerson().getNames());
                
                return new SubsidiarySignerListDTO(
                    subsidiary.getPublicId(),
                    subsidiary.getName(),
                    employee.getPersonDocumentNumber(),
                    employeeName + " (Nivel Empresa)",
                    signer.getResponsiblePosition(),
                    getSignatureImageUrl(signer),
                    true
                );
            }
            
            return new SubsidiarySignerListDTO(
                subsidiary.getPublicId(),
                subsidiary.getName(),
                null,
                null,
                null,
                null,
                false
            );
        }
    }

    /**
     * Obtiene los detalles del responsable de firma para una subsidiaria
     */
    @Transactional(readOnly = true)
    public SubsidiarySignerDetailsDTO getSignerBySubsidiary(UUID subsidiaryPublicId) {
        log.info("Fetching signer details for subsidiary: {}", subsidiaryPublicId);
        
        SubsidiaryEntity subsidiary = subsidiaryRepository.findByPublicId(subsidiaryPublicId)
            .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.subsidiary.not-found", subsidiaryPublicId));
        
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        
        Optional<CompanySubsidiarySignerEntity> signerOpt = signerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiary.getId());
        
        if (signerOpt.isEmpty()) {
            // Buscar a nivel de empresa
            signerOpt = signerRepository.findLatestByCompany(company.getId());
        }
        
        if (signerOpt.isEmpty()) {
            throw new IdentifierNotFoundException("exception.organization.signer.not-found", subsidiaryPublicId);
        }
        
        CompanySubsidiarySignerEntity signer = signerOpt.get();
        EmployeeEntity employee = signer.getResponsibleEmployee();
        String employeeName = String.format("%s %s, %s",
            employee.getPerson().getPaternalLastname(),
            employee.getPerson().getMaternalLastname(),
            employee.getPerson().getNames());
        
        return new SubsidiarySignerDetailsDTO(
            signer.getPublicId(),
            signer.getSubsidiary() != null ? signer.getSubsidiary().getPublicId() : null,
            signer.getSubsidiary() != null ? signer.getSubsidiary().getName() : "Nivel Empresa",
            employee.getPersonDocumentNumber(),
            employeeName,
            signer.getResponsiblePosition(),
            signer.getSignatureImageUrl(),
            signer.getNotes(),
            signer.getCreatedAt(),
            signer.getCreatedBy()
        );
    }

    /**
     * Crea o actualiza un responsable de firma para una subsidiaria
     * Siempre crea un nuevo registro histórico (no actualiza el existente)
     */
    @Transactional
    public SubsidiarySignerDetailsDTO createOrUpdateSigner(CreateSubsidiarySignerRequest request, MultipartFile signatureImage) {
        log.info("Creating signer for subsidiary: {} with employee: {}", 
            request.subsidiaryPublicId(), request.responsibleEmployeeDocumentNumber());
        
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        
        SubsidiaryEntity subsidiary = null;
        if (request.subsidiaryPublicId() != null) {
            subsidiary = subsidiaryRepository.findByPublicId(request.subsidiaryPublicId())
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.subsidiary.not-found", request.subsidiaryPublicId()));
        }
        
        EmployeeEntity employee = employeeUseCase.findByPersonDocumentNumber(request.responsibleEmployeeDocumentNumber())
            .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.employee.not-found-by-document", request.responsibleEmployeeDocumentNumber()));
        
        // Crear nuevo registro histórico
        CompanySubsidiarySignerEntity newSigner = CompanySubsidiarySignerEntity.builder()
            .company(company)
            .subsidiary(subsidiary)
            .responsibleEmployee(employee)
            .responsiblePosition(request.responsiblePosition())
            .signatureImageUrl(request.signatureImageUrl()) // Mantener para compatibilidad, pero se usará el archivo interno
            .notes(request.notes())
            .build();
        
        CompanySubsidiarySignerEntity savedSigner = signerRepository.save(newSigner);
        log.info("Successfully created signer with public ID: {}", savedSigner.getPublicId());
        
        // Guardar archivo de firma si se proporciona y actualizar signatureImageUrl en la entidad
        String signatureImageUrl = request.signatureImageUrl();
        if (signatureImage != null && !signatureImage.isEmpty()) {
            try {
                InternalFileEntity file = fileStorageService.saveFile(
                    savedSigner,
                    signatureImage,
                    FileCategory.SIGNATURE.getCode(),
                    "Firma del responsable de boletas de pago"
                );
                signatureImageUrl = "/v1/internal-files/" + file.getPublicId() + "/download";
                // Actualizar el campo signatureImageUrl en la entidad con la URL de descarga
                savedSigner.setSignatureImageUrl(signatureImageUrl);
                savedSigner = signerRepository.save(savedSigner);
                log.info("Firma guardada como archivo interno con publicId: {} y URL guardada en entidad", file.getPublicId());
            } catch (Exception e) {
                log.error("Error guardando archivo de firma", e);
                throw new RuntimeException("Error guardando archivo de firma: " + e.getMessage(), e);
            }
        }
        
        String employeeName = String.format("%s %s, %s",
            employee.getPerson().getPaternalLastname(),
            employee.getPerson().getMaternalLastname(),
            employee.getPerson().getNames());
        
        return new SubsidiarySignerDetailsDTO(
            savedSigner.getPublicId(),
            savedSigner.getSubsidiary() != null ? savedSigner.getSubsidiary().getPublicId() : null,
            savedSigner.getSubsidiary() != null ? savedSigner.getSubsidiary().getName() : "Nivel Empresa",
            employee.getPersonDocumentNumber(),
            employeeName,
            savedSigner.getResponsiblePosition(),
            savedSigner.getSignatureImageUrl(), // Usar el valor guardado en la entidad
            savedSigner.getNotes(),
            savedSigner.getCreatedAt(),
            savedSigner.getCreatedBy()
        );
    }

    /**
     * Actualiza un responsable de firma para una subsidiaria
     * Crea un nuevo registro histórico (no actualiza el existente)
     * Permite cambiar empleado, cargo, firma o notas
     */
    @Transactional
    public SubsidiarySignerDetailsDTO updateSigner(UUID signerPublicId, UpdateSubsidiarySignerRequest request, MultipartFile signatureImage) {
        log.info("Updating signer with public ID: {}", signerPublicId);
        
        // Buscar el signer existente
        CompanySubsidiarySignerEntity existingSigner = signerRepository.findByPublicId(signerPublicId)
            .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.signer.not-found-by-id", signerPublicId));
        
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        
        // Determinar el empleado: usar el nuevo si se proporciona, sino mantener el existente
        EmployeeEntity employee = existingSigner.getResponsibleEmployee();
        if (request.responsibleEmployeeDocumentNumber() != null && !request.responsibleEmployeeDocumentNumber().isEmpty()) {
            employee = employeeUseCase.findByPersonDocumentNumber(request.responsibleEmployeeDocumentNumber())
                .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.employee.not-found-by-document", request.responsibleEmployeeDocumentNumber()));
        }
        
        // Determinar el cargo: usar el nuevo si se proporciona, sino mantener el existente
        String position = request.responsiblePosition() != null && !request.responsiblePosition().isEmpty()
            ? request.responsiblePosition()
            : existingSigner.getResponsiblePosition();
        
        // Obtener la URL de firma: usar la del request si se proporciona, sino obtenerla del signer existente
        String signatureImageUrl = request.signatureImageUrl() != null 
            ? request.signatureImageUrl() 
            : getSignatureImageUrl(existingSigner);
        
        // Crear nuevo registro histórico con los datos actualizados
        CompanySubsidiarySignerEntity newSigner = CompanySubsidiarySignerEntity.builder()
            .company(company)
            .subsidiary(existingSigner.getSubsidiary()) // Mantener la misma subsidiaria
            .responsibleEmployee(employee)
            .responsiblePosition(position)
            .signatureImageUrl(signatureImageUrl) // Guardar la URL (puede ser de archivo interno o legacy)
            .notes(request.notes() != null ? request.notes() : existingSigner.getNotes())
            .build();
        
        CompanySubsidiarySignerEntity savedSigner = signerRepository.save(newSigner);
        log.info("Successfully updated signer with public ID: {}", savedSigner.getPublicId());
        
        // Guardar archivo de firma si se proporciona y actualizar signatureImageUrl en la entidad
        if (signatureImage != null && !signatureImage.isEmpty()) {
            try {
                InternalFileEntity file = fileStorageService.saveFile(
                    savedSigner,
                    signatureImage,
                    FileCategory.SIGNATURE.getCode(),
                    "Firma del responsable de boletas de pago"
                );
                signatureImageUrl = "/v1/internal-files/" + file.getPublicId() + "/download";
                // Actualizar el campo signatureImageUrl en la entidad con la URL de descarga
                savedSigner.setSignatureImageUrl(signatureImageUrl);
                savedSigner = signerRepository.save(savedSigner);
                log.info("Firma guardada como archivo interno con publicId: {} y URL guardada en entidad", file.getPublicId());
            } catch (Exception e) {
                log.error("Error guardando archivo de firma", e);
                throw new RuntimeException("Error guardando archivo de firma: " + e.getMessage(), e);
            }
        }
        
        String employeeName = String.format("%s %s, %s",
            employee.getPerson().getPaternalLastname(),
            employee.getPerson().getMaternalLastname(),
            employee.getPerson().getNames());
        
        return new SubsidiarySignerDetailsDTO(
            savedSigner.getPublicId(),
            savedSigner.getSubsidiary() != null ? savedSigner.getSubsidiary().getPublicId() : null,
            savedSigner.getSubsidiary() != null ? savedSigner.getSubsidiary().getName() : "Nivel Empresa",
            employee.getPersonDocumentNumber(),
            employeeName,
            savedSigner.getResponsiblePosition(),
            savedSigner.getSignatureImageUrl(), // Usar el valor guardado en la entidad
            savedSigner.getNotes(),
            savedSigner.getCreatedAt(),
            savedSigner.getCreatedBy()
        );
    }

    /**
     * Elimina (soft delete) el responsable de firma más reciente de una subsidiaria
     */
    @Transactional
    public void deleteSigner(UUID subsidiaryPublicId) {
        log.info("Deleting signer for subsidiary: {}", subsidiaryPublicId);
        
        SubsidiaryEntity subsidiary = subsidiaryRepository.findByPublicId(subsidiaryPublicId)
            .orElseThrow(() -> new IdentifierNotFoundException("exception.organization.subsidiary.not-found", subsidiaryPublicId));
        
        CompanyEntity company = companyUseCase.getPrimaryCompanyEntity();
        
        Optional<CompanySubsidiarySignerEntity> signerOpt = signerRepository
            .findLatestByCompanyAndSubsidiary(company.getId(), subsidiary.getId());
        
        if (signerOpt.isEmpty()) {
            throw new IdentifierNotFoundException("exception.organization.signer.not-found", subsidiaryPublicId);
        }
        
        // Convertir String a Long para softDelete
        signerRepository.softDelete(Long.parseLong(signerOpt.get().getId()), "SYSTEM");
        log.info("Successfully deleted signer for subsidiary: {}", subsidiaryPublicId);
    }

    /**
     * Obtiene la URL de la imagen de firma desde archivos internos o URL legacy
     */
    private String getSignatureImageUrl(CompanySubsidiarySignerEntity signer) {
        // Primero intentar obtener desde archivos internos
        try {
            List<InternalFileEntity> files = fileStorageService.getFilesByFileableAndCategory(
                signer, FileCategory.SIGNATURE.getCode());
            if (!files.isEmpty()) {
                InternalFileEntity file = files.get(0);
                return "/v1/internal-files/" + file.getPublicId() + "/download";
            }
        } catch (Exception e) {
            log.debug("No se encontró archivo interno de firma para signer {}", signer.getPublicId());
        }
        
        // Fallback a URL legacy (Cloudinary)
        return signer.getSignatureImageUrl();
    }
}

