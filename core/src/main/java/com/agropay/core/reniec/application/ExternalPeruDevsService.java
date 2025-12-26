package com.agropay.core.reniec.application;

import com.agropay.core.sunat.application.ICompanyExternalProvider;
import com.agropay.core.reniec.http.ReniecRestClient;
import com.agropay.core.sunat.http.SunatRestClient;
import com.agropay.core.sunat.models.CompanyExternalInfo;
import com.agropay.core.reniec.models.PersonExternalInfo;
import com.agropay.core.sunat.models.PeruDevsCompanyDTO;
import com.agropay.core.reniec.models.PeruDevsPersonDTO;
import com.agropay.core.shared.exceptions.ProvidersExternalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalPeruDevsService implements ICompanyExternalProvider, IPersonExternalProvider {

    private final ReniecRestClient reniecClient;
    private final SunatRestClient sunatClient;

    @Override
    public Optional<PersonExternalInfo> getPerson(String dni) {
        try {
            log.info("Calling RENIEC with DNI: {}", dni);
            PeruDevsPersonDTO response = reniecClient.getPersonByDni(dni);

            // Log la respuesta RAW
            log.info("RAW Response: {}", response);
            log.info("Estado: {}, Mensaje: {}", response.estado(), response.mensaje());

            if (!response.estado() || response.resultado() == null) {
                log.warn("Person with DNI {} not found in RENIEC service or response was invalid.", dni);
                return Optional.empty();
            }

            PeruDevsPersonDTO.PeruDevsPersonDetailDTO detail = response.resultado();
            PersonExternalInfo personInfo = PersonExternalInfo.builder()
                    .dni(detail.id())
                    .paternalLastname(detail.apellido_paterno())
                    .maternalLastname(detail.apellido_materno())
                    .fullName(detail.nombre_completo())
                    .gender(String.valueOf(detail.genero()))
                    .birthDate(detail.fecha_nacimiento())
                    .build();

            log.info("Successfully found person with DNI {}.", dni);
            return Optional.of(personInfo);

        } catch (RestClientException e) {
            log.error("A REST client error occurred while calling RENIEC service for DNI {}: {}", dni, e.getMessage());
            throw new ProvidersExternalException("exception.external-service.providers.not-working");
        } catch (Exception e) {
            log.error("ERROR COMPLETO: {}", e.getMessage());
            log.error("Tipo de error: {}", e.getClass().getSimpleName());
            e.printStackTrace(); // Para ver el stack trace completo
            throw new ProvidersExternalException("exception.external-service.providers.not-working");
        }
    }

    @Override
    public Optional<CompanyExternalInfo> getCompany(String ruc) {
        try {
            log.info("Attempting to fetch company with RUC {} from SUNAT service.", ruc);
            PeruDevsCompanyDTO response = sunatClient.getCompanyByRuc(ruc);

            if (response == null || !response.estado() || response.resultado() == null) {
                log.warn("Company with RUC {} not found in SUNAT service or response was invalid.", ruc);
                return Optional.empty();
            }

            PeruDevsCompanyDTO.PeruDevsCompanyDetailDTO detail = response.resultado();
            CompanyExternalInfo companyInfo = CompanyExternalInfo.builder()
                    .ruc(detail.id())
                    .businessName(detail.razon_social())
                    .tradeName(detail.nombre_comercial())
                    .status(detail.condicion()) // e.g., HABIDO
                    .companyType(detail.tipo()) // e.g., SOC. ANON. CERRADA
                    .build();

            log.info("Successfully found company with RUC {}.", ruc);
            return Optional.of(companyInfo);

        } catch (RestClientException e) {
            log.error("A REST client error occurred while calling SUNAT service for RUC {}: {}", ruc, e.getMessage());
            throw new ProvidersExternalException("exception.external-service.providers.not-working");
        } catch (Exception e) {
            log.error("An unexpected error occurred in ExternalPeruDevsService for RUC {}: {}", ruc, e.getMessage(), e);
            throw new ProvidersExternalException("exception.external-service.providers.not-working");
        }
    }
}
