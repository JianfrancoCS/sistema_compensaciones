package com.agropay.core.organization.web;

import com.agropay.core.organization.application.usecase.IPersonUseCase;
import com.agropay.core.organization.mapper.IPersonMapper;
import com.agropay.core.organization.model.person.*;
import com.agropay.core.organization.model.documenttype.DocumentTypeSelectOptionDTO;
import com.agropay.core.organization.domain.PersonEntity;
import com.agropay.core.organization.constant.DocumentTypeEnum;
import com.agropay.core.shared.utils.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(PersonController.BASE_URL)
@RequiredArgsConstructor
@Tag(name = "Gestión de Personas", description = "Operaciones para consultar y gestionar la información de personas naturales (empleados, contactos, etc.).")
@Validated
@Slf4j
public class PersonController {

    public static final String BASE_URL = "/v1/persons";

    private final IPersonUseCase personUseCase;
    private final IPersonMapper personMapper;

    @GetMapping("/{documentNumber}")
    @Operation(summary = "Buscar una persona por número de documento",
               description = "Obtiene los detalles completos de una persona registrada en el sistema. Si no se especifica isNational, busca primero en BD, luego en RENIEC. Si isNational=false, busca en BD y si no existe, consulta Verifica.id API (requiere birthdate).")
    public ResponseEntity<ApiResult<PersonDetailsDTO>> findById(
            @PathVariable("documentNumber") String documentNumber,
            @RequestParam(required = false) Boolean isNational,
            @RequestParam(required = false) String birthdate) {

        PersonEntity entity;

        if (isNational != null && !isNational) {
            // Búsqueda de extranjero: BD -> Verifica.id API (si se proporciona birthdate)
            if (birthdate != null && !birthdate.trim().isEmpty()) {
                // Si se proporciona birthdate, intentar buscar/crear desde API externa
                entity = ((com.agropay.core.organization.application.services.PersonService) personUseCase)
                        .findOrCreateForeignPersonByDocument(documentNumber, birthdate);
            } else {
                // Solo buscar en BD
                entity = personUseCase.findPersonByDocumentNumber(documentNumber, false);
            }
        } else if (isNational != null && isNational) {
            // Búsqueda de nacional: solo en BD
            entity = personUseCase.findPersonByDocumentNumber(documentNumber, true);
        } else {
            // Búsqueda normal: BD -> RENIEC -> Error
            entity = personUseCase.findOrCreatePersonByDni(documentNumber);
        }

        boolean personIsNational = DocumentTypeEnum.DNI.getCode().equals(entity.getDocumentType().getCode());
        PersonDetailsDTO details = new PersonDetailsDTO(
                entity.getDocumentNumber(),
                entity.getNames(),
                entity.getPaternalLastname(),
                entity.getMaternalLastname(),
                entity.getDob(),
                entity.getGender(),
                entity.getDistrict() != null ? entity.getDistrict().getPublicId() : null,
                personIsNational
        );
        return ResponseEntity.ok(ApiResult.success(details));
    }

    @PatchMapping("/{documentNumber}")
    @Operation(summary = "Actualizar la dirección de una persona", description = "Actualiza la información de la dirección de una persona existente, identificada por su número de documento.")
    public ResponseEntity<ApiResult<CommandPersonResponse>> update(@RequestBody @Valid UpdateAddressPersonRequest request, @PathVariable("documentNumber") String documentNumber) {
        return ResponseEntity.ok(ApiResult.success(personUseCase.updateAddressPerson(request, documentNumber)));
    }

    @PostMapping("/foreign")
    @Operation(summary = "Crear persona extranjera manualmente", description = "Permite crear manualmente una persona extranjera (con carnet de extranjería) sin consultar servicios externos.")
    public ResponseEntity<ApiResult<CommandPersonResponse>> createForeignPerson(@RequestBody @Valid CreatePersonManualRequest request) {
        log.info("Solicitud REST para crear persona extranjera: {}", request.documentNumber());
        CommandPersonResponse response = personUseCase.createPersonManual(request);
        return new ResponseEntity<>(ApiResult.success(response, "Persona extranjera creada exitosamente"), HttpStatus.CREATED);
    }

    @PutMapping("/foreign/{documentNumber}")
    @Operation(summary = "Actualizar persona extranjera", description = "Permite actualizar los datos de una persona extranjera existente.")
    public ResponseEntity<ApiResult<CommandPersonResponse>> updateForeignPerson(
            @PathVariable String documentNumber,
            @RequestBody @Valid UpdatePersonManualRequest request) {
        log.info("Solicitud REST para actualizar persona extranjera: {}", documentNumber);
        CommandPersonResponse response = personUseCase.updatePersonManual(documentNumber, request);
        return ResponseEntity.ok(ApiResult.success(response, "Persona extranjera actualizada exitosamente"));
    }

    @GetMapping("/document-types/select-options")
    @Operation(summary = "Obtener tipos de documento para selección", description = "Devuelve una lista de tipos de documento disponibles para usar en formularios de selección.")
    public ResponseEntity<ApiResult<List<DocumentTypeSelectOptionDTO>>> getDocumentTypesForSelect() {
        List<DocumentTypeSelectOptionDTO> response = personUseCase.getDocumentTypesForSelect();
        return ResponseEntity.ok(ApiResult.success(response));
    }
}
