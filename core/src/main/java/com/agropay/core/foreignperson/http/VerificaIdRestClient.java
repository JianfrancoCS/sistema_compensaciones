package com.agropay.core.foreignperson.http;

import com.agropay.core.foreignperson.models.VerificaIdResponseDTO;
import com.agropay.core.shared.config.ExternalServicesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificaIdRestClient {

    private final ExternalServicesConfig externalServicesConfig;
    private final RestClient.Builder restClientBuilder;

    public VerificaIdResponseDTO getForeignPersonByDocument(String documentNumber, String birthdate) {
        String baseUrl = externalServicesConfig.getForeignPerson().getVerificaId().getBaseUrl();
        String apiKey = externalServicesConfig.getForeignPerson().getVerificaId().getApiKey();

        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        // Preparar parámetros del formulario
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("document_number", documentNumber);
        formData.add("birthdate", birthdate);

        try {
            log.info("Calling Verifica.id API for foreign person with document: {} and birthdate: {}", documentNumber, birthdate);
            
            VerificaIdResponseDTO response = restClient.post()
                    .uri("/v2/consulta/extranjeros")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(VerificaIdResponseDTO.class);

            if (response == null) {
                log.warn("Verifica.id API returned null response for document: {}", documentNumber);
                throw new RestClientException("Null response from Verifica.id API");
            }

            log.info("Verifica.id API response - Status: {}, Message: {}", response.status(), response.message());
            return response;
        } catch (RestClientException e) {
            log.error("Error calling Verifica.id API for document {}: {}", documentNumber, e.getMessage());
            throw new RuntimeException("Error calling Verifica.id API", e);
        }
    }
}

