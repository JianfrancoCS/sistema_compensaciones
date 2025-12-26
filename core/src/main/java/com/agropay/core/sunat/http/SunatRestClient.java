package com.agropay.core.sunat.http;

import com.agropay.core.shared.config.ExternalServicesConfig;
import com.agropay.core.sunat.models.PeruDevsCompanyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class SunatRestClient {

    private final ExternalServicesConfig externalServicesConfig;
    private final RestClient.Builder restClientBuilder;

    public PeruDevsCompanyDTO getCompanyByRuc(String ruc) {
        String baseUrl = externalServicesConfig.getSunat().getPeruDevs().getBaseUrl();
        String apiKey = externalServicesConfig.getSunat().getPeruDevs().getApiKey();

        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/ruc")
                            .queryParam("document", ruc)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(PeruDevsCompanyDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling SUNAT service", e);
        }
    }
}

