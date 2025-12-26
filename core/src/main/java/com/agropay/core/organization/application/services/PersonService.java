package com.agropay.core.organization.application.services;

import com.agropay.core.organization.application.usecase.IDistrictUseCase;
import com.agropay.core.organization.application.usecase.IDocumentTypeUseCase;
import com.agropay.core.organization.application.usecase.IPersonUseCase;
import com.agropay.core.organization.constant.DocumentTypeEnum;
import com.agropay.core.organization.domain.DistrictEntity;
import com.agropay.core.organization.domain.DocumentTypeEntity;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.exception.PersonIdentityMismatchException;
import com.agropay.core.organization.mapper.IPersonMapper;
import com.agropay.core.organization.model.documenttype.DocumentTypeSelectOptionDTO;
import com.agropay.core.organization.model.person.CommandPersonResponse;
import com.agropay.core.organization.model.person.CreatePersonManualRequest;
import com.agropay.core.organization.model.person.UpdateAddressPersonRequest;
import com.agropay.core.organization.model.person.UpdatePersonManualRequest;
import com.agropay.core.organization.persistence.IPersonRepository;
import com.agropay.core.organization.persistence.PersonSpecification;
import com.agropay.core.foreignperson.application.IForeignPersonExternalProvider;
import com.agropay.core.foreignperson.models.ForeignPersonExternalInfo;
import com.agropay.core.reniec.application.IPersonExternalProvider;
import com.agropay.core.reniec.models.PersonExternalInfo;
import com.agropay.core.shared.exceptions.BusinessValidationException;
import com.agropay.core.shared.exceptions.IdentifierNotFoundException;
import com.agropay.core.shared.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService implements IPersonUseCase {

    private final IPersonRepository personRepository;
    private final IPersonExternalProvider personExternalProvider;
    private final IForeignPersonExternalProvider foreignPersonExternalProvider;
    private final IPersonMapper personMapper;
    private final IDistrictUseCase districtUseCase;
    private final IDocumentTypeUseCase documentTypeUseCase;

    @Transactional
    public PersonEntity findOrCreatePersonByDni(String documentNumber) {
        log.info("Attempting to find or create a person with documentNumber: {}", documentNumber);

        Optional<PersonEntity> optPerson = personRepository.findById(documentNumber);
        if (optPerson.isPresent()) {
            log.info("Person with documentNumber: {} found in local database.", documentNumber);
            PersonEntity existingPerson = optPerson.get();
            // CORRECCIÓN: Retornar PersonEntity directamente
            return existingPerson;
        }

        // 2. Not found locally, validate with external provider
        log.info("Person with documentNumber: {} not found locally. Fetching from external provider for validation.", documentNumber);
        // This method will throw PersonNotFoundInExternalServiceException if not found, which is the desired behavior.
        PersonExternalInfo externalInfo = fetchExternalPerson(documentNumber);

        log.info("Person with documentNumber: {} validated externally. Creating local record.", documentNumber);

        // Obtener tipo de documento DNI por defecto
        DocumentTypeEntity dniDocumentType = documentTypeUseCase.getDniDocumentType();

        PersonEntity newPerson = PersonEntity
                .builder()
                .documentNumber(documentNumber)
                .documentType(dniDocumentType)
                .names(externalInfo.fullName())
                .paternalLastname(externalInfo.paternalLastname())
                .maternalLastname(externalInfo.maternalLastname())
                .dob(DateUtils.parseFlexible(externalInfo.birthDate()))
                .gender(externalInfo.gender())
                .build();
        PersonEntity save = personRepository.save(newPerson);
        // CORRECCIÓN: Retornar PersonEntity directamente
        return save;
    }

    public PersonEntity getByIdentifier(String documentNumber) {
        log.debug("Attempting to find person entity with documentNumber: {}", documentNumber);
        return personRepository.findById(documentNumber)
                .orElseThrow(() -> {
                    log.warn("Person with documentNumber {} not found.", documentNumber);
                    return new IdentifierNotFoundException("exception.shared.identifier-not-found", documentNumber);
                });
    }

    @Override
    @Transactional
    public CommandPersonResponse updateAddressPerson(UpdateAddressPersonRequest request, String documentNumber) {
        log.info("Attempting to update address for person with documentNumber: {}", documentNumber);
        PersonEntity entity = getByIdentifier(documentNumber);
        DistrictEntity district = districtUseCase.findByPublicId(request.districtPublicId());
        entity.setDistrict(district);
        log.info("Successfully updated address for person with documentNumber: {}", documentNumber);
        return new CommandPersonResponse(
                entity.getDocumentNumber(),
                entity.getNames(),
                entity.getPaternalLastname(),
                entity.getMaternalLastname(),
                entity.getGender(),
                isNational(entity)
        );
    }

    @Transactional(readOnly = true)
    public PersonExternalInfo fetchExternalPerson(String documentNumber) {
        log.info("Fetching external person information for documentNumber: {}", documentNumber);
        return personExternalProvider.getPerson(documentNumber)
                .orElseThrow(() -> {
                    log.warn("Person with documentNumber {} not found in any external provider.", documentNumber);
                    return new IdentifierNotFoundException("exception.external-service.person.not-found", documentNumber);
                });
    }


    private List<String> findMismatchedFields(
            String names,
            String paternalLastname,
            String maternalLastname,
            String documentNumber,
            LocalDate dob,
            PersonEntity person
    ) {
        List<String> mismatches = new ArrayList<>();

        if (person == null) {
            mismatches.add("person (is null)");
            return mismatches;
        }

        if (!Objects.equals(person.getNames(), names)) { // CAMBIO AQUÍ: Usar getNames()
            mismatches.add("names");
        }
        if (!Objects.equals(person.getPaternalLastname(), paternalLastname)) { // CAMBIO AQUÍ: Usar getPaternalLastname()
            mismatches.add("paternalLastname");
        }
        if (!Objects.equals(person.getMaternalLastname(), maternalLastname)) { // CAMBIO AQUÍ: Usar getMaternalLastname()
            mismatches.add("maternalLastname");
        }
        if (!Objects.equals(person.getDocumentNumber(), documentNumber)) { // CAMBIO AQUÍ: Usar getDocumentNumber()
            mismatches.add("documentNumber");
        }
        if (!Objects.equals(person.getDob(), dob)) { // CAMBIO AQUÍ: Usar getDob()
            mismatches.add("dob");
        }

        return mismatches;
    }

    @Override // Añadir @Override ya que implementa IPersonAPI
    public void validateValueOfPerson(
            String names,
            String paternalLastname,
            String maternalLastname,
            String documentNumber,
            LocalDate dob,
            PersonEntity person // CAMBIO AQUÍ: De PersonApiDTO a PersonEntity
    ) {
        List<String> mismatches = findMismatchedFields(
                names,
                paternalLastname,
                maternalLastname,
                documentNumber,
                dob,
                person
        );

        if (!mismatches.isEmpty()) {
                throw new PersonIdentityMismatchException(
                        "exception.person.identity-mismatch",
                        String.join(", ", mismatches)
                );
        }
    }

    @Override
    @Transactional
    public CommandPersonResponse createPersonManual(CreatePersonManualRequest request) {
        log.info("Creating person manually with document number: {}", request.documentNumber());

        // Verificar que la persona no exista ya
        if (personRepository.findById(request.documentNumber()).isPresent()) {
            throw new BusinessValidationException("exception.organization.employee.already-exists", request.documentNumber());
        }

        // Obtener tipo de documento CE automáticamente
        DocumentTypeEntity documentType = documentTypeUseCase.getForeignDocumentType();

        // Validar longitud del documento (CE = 9 dígitos)
        documentTypeUseCase.validateDocumentLength(request.documentNumber(), documentType);

        // Crear la persona (sin distrito por defecto)
        PersonEntity person = PersonEntity.builder()
                .documentNumber(request.documentNumber())
                .documentType(documentType)
                .names(request.names())
                .paternalLastname(request.paternalLastname())
                .maternalLastname(request.maternalLastname())
                .dob(request.dateOfBirth())
                .build();

        PersonEntity savedPerson = personRepository.save(person);
        log.info("Person created manually with document number: {}", savedPerson.getDocumentNumber());

        return new CommandPersonResponse(
                savedPerson.getDocumentNumber(),
                savedPerson.getNames(),
                savedPerson.getPaternalLastname(),
                savedPerson.getMaternalLastname(),
                savedPerson.getGender(),
                isNational(savedPerson)
        );
    }

    @Override
    @Transactional
    public CommandPersonResponse updatePersonManual(String documentNumber, UpdatePersonManualRequest request) {
        log.info("Updating person manually with document number: {}", documentNumber);

        // Buscar solo personas extranjeras (isNational = false)
        PersonEntity person = findPersonByDocumentNumber(documentNumber, false);

        // Actualizar campos (sin distrito)
        person.setNames(request.names());
        person.setPaternalLastname(request.paternalLastname());
        person.setMaternalLastname(request.maternalLastname());
        person.setDob(request.dateOfBirth());

        PersonEntity updatedPerson = personRepository.save(person);
        log.info("Person updated manually with document number: {}", updatedPerson.getDocumentNumber());

        return new CommandPersonResponse(
                updatedPerson.getDocumentNumber(),
                updatedPerson.getNames(),
                updatedPerson.getPaternalLastname(),
                updatedPerson.getMaternalLastname(),
                updatedPerson.getGender(),
                isNational(updatedPerson)
        );
    }

    @Override
    public List<DocumentTypeSelectOptionDTO> getDocumentTypesForSelect() {
        return documentTypeUseCase.getDocumentTypesForSelect();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonEntity findPersonByDocumentNumber(String documentNumber, Boolean isNational) {
        log.info("Searching for person with documentNumber: {} and isNational: {}", documentNumber, isNational);

        // Primero buscar en la base de datos local
        Specification<PersonEntity> spec = PersonSpecification.buildSpecification(documentNumber, isNational);
        Optional<PersonEntity> optPerson = personRepository.findOne(spec);
        
        if (optPerson.isPresent()) {
            log.info("Person with documentNumber: {} found in local database.", documentNumber);
            return optPerson.get();
        }

        // Si no se encuentra y es extranjero, intentar buscar en la API externa
        // Nota: Para extranjeros, necesitamos birthdate, pero si no se proporciona, 
        // solo retornamos error (la búsqueda externa requiere birthdate)
        if (isNational != null && !isNational) {
            log.info("Foreign person with documentNumber: {} not found locally. External API requires birthdate.", documentNumber);
            throw new IdentifierNotFoundException("exception.shared.identifier-not-found", documentNumber);
        }

        // Si es nacional o no se especifica, buscar en RENIEC
        if (isNational == null || isNational) {
            log.info("Person with documentNumber: {} not found locally. Will be fetched from RENIEC when creating.", documentNumber);
        }

        throw new IdentifierNotFoundException("exception.shared.identifier-not-found", documentNumber);
    }

    /**
     * Busca o crea una persona extranjera consultando la API de Verifica.id
     * @param documentNumber Número de documento (CE)
     * @param birthdate Fecha de nacimiento en formato YYYY-MM-DD
     * @return PersonEntity creada o encontrada
     */
    @Transactional
    public PersonEntity findOrCreateForeignPersonByDocument(String documentNumber, String birthdate) {
        log.info("Attempting to find or create a foreign person with documentNumber: {} and birthdate: {}", documentNumber, birthdate);

        // Buscar primero en la base de datos local
        Specification<PersonEntity> spec = PersonSpecification.buildSpecification(documentNumber, false);
        Optional<PersonEntity> optPerson = personRepository.findOne(spec);
        
        if (optPerson.isPresent()) {
            log.info("Foreign person with documentNumber: {} found in local database.", documentNumber);
            return optPerson.get();
        }

        // No encontrada localmente, consultar API externa
        log.info("Foreign person with documentNumber: {} not found locally. Fetching from Verifica.id API.", documentNumber);
        ForeignPersonExternalInfo externalInfo = fetchForeignExternalPerson(documentNumber, birthdate);

        log.info("Foreign person with documentNumber: {} validated externally. Creating local record.", documentNumber);

        // Obtener tipo de documento CE
        DocumentTypeEntity ceDocumentType = documentTypeUseCase.getForeignDocumentType();

        PersonEntity newPerson = PersonEntity.builder()
                .documentNumber(documentNumber)
                .documentType(ceDocumentType)
                .names(externalInfo.nombres())
                .paternalLastname(externalInfo.paternalLastname())
                .maternalLastname(externalInfo.maternalLastname())
                .dob(DateUtils.parseFlexible(externalInfo.birthDate()))
                .build();

        PersonEntity savedPerson = personRepository.save(newPerson);
        log.info("Foreign person created with documentNumber: {}", savedPerson.getDocumentNumber());
        return savedPerson;
    }

    @Transactional(readOnly = true)
    public ForeignPersonExternalInfo fetchForeignExternalPerson(String documentNumber, String birthdate) {
        log.info("Fetching external foreign person information for documentNumber: {} and birthdate: {}", documentNumber, birthdate);
        return foreignPersonExternalProvider.getForeignPerson(documentNumber, birthdate)
                .orElseThrow(() -> {
                    log.warn("Foreign person with documentNumber {} and birthdate {} not found in Verifica.id API.", documentNumber, birthdate);
                    return new IdentifierNotFoundException("exception.external-service.person.not-found", documentNumber);
                });
    }

    private boolean isNational(PersonEntity person) {
        return DocumentTypeEnum.DNI.getCode().equals(person.getDocumentType().getCode());
    }

}