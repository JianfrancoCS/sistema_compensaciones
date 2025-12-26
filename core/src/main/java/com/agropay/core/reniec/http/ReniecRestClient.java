package com.agropay.core.reniec.http;

import com.agropay.core.reniec.models.PeruDevsPersonDTO;
import com.agropay.core.shared.config.ExternalServicesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class ReniecRestClient {

    private final ExternalServicesConfig externalServicesConfig;
    private final RestClient.Builder restClientBuilder;

    public PeruDevsPersonDTO getPersonByDni(String dni) {
        String baseUrl = externalServicesConfig.getReniec().getPeruDevs().getBaseUrl();
        String apiKey = externalServicesConfig.getReniec().getPeruDevs().getApiKey();

        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/dni/complete")
                            .queryParam("document", dni)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(PeruDevsPersonDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling RENIEC service", e);
        }
    }
}

