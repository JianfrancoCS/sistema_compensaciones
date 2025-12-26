package com.agropay.core.foreignperson.application;

import com.agropay.core.foreignperson.http.VerificaIdRestClient;
import com.agropay.core.foreignperson.models.ForeignPersonExternalInfo;
import com.agropay.core.foreignperson.models.VerificaIdResponseDTO;
import com.agropay.core.shared.exceptions.ProvidersExternalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalVerificaIdService implements IForeignPersonExternalProvider {

    private final VerificaIdRestClient verificaIdRestClient;

    @Override
    public Optional<ForeignPersonExternalInfo> getForeignPerson(String documentNumber, String birthdate) {
        try {
            log.info("Calling Verifica.id API with document: {} and birthdate: {}", documentNumber, birthdate);
            VerificaIdResponseDTO response = verificaIdRestClient.getForeignPersonByDocument(documentNumber, birthdate);

            if (response == null || response.status() == null || response.status() != 200) {
                log.warn("Foreign person with document {} not found in Verifica.id service or response was invalid. Status: {}, Message: {}", 
                        documentNumber, response != null ? response.status() : "null", response != null ? response.message() : "null");
                return Optional.empty();
            }

            if (response.data() == null) {
                log.warn("Verifica.id response for document {} has no data", documentNumber);
                return Optional.empty();
            }

            VerificaIdResponseDTO.VerificaIdDataDTO data = response.data();
            ForeignPersonExternalInfo personInfo = ForeignPersonExternalInfo.builder()
                    .documentNumber(data.numeroDeDocumento())
                    .nombres(data.nombres())
                    .paternalLastname(data.apellidoPaterno())
                    .maternalLastname(data.apellidoMaterno())
                    .birthDate(data.fechaNacimiento())
                    .nacionalidad(data.nacionalidad())
                    .calidadMigratoria(data.calidadMigratoria())
                    .fechaExpiracionResidencia(data.fechaExpiracionResidencia())
                    .fechaExpiracionCarnet(data.fechaExpiracionCarnet())
                    .fechaUltimaEmisionCarnet(data.fechaUltimaEmisionCarnet())
                    .build();

            log.info("Successfully found foreign person with document {}.", documentNumber);
            return Optional.of(personInfo);

        } catch (RestClientException e) {
            log.error("A REST client error occurred while calling Verifica.id service for document {}: {}", documentNumber, e.getMessage());
            throw new ProvidersExternalException("exception.external-service.providers.not-working");
        } catch (Exception e) {
            log.error("Unexpected error while calling Verifica.id service for document {}: {}", documentNumber, e.getMessage(), e);
            return Optional.empty();
        }
    }
}

