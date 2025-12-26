package com.agropay.core.shared.config;

import com.agropay.core.foreignperson.config.ForeignPersonProvidersConfig;
import com.agropay.core.reniec.config.ReniecProvidersConfig;
import com.agropay.core.sunat.config.SunatProvidersConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(value = "external-services")
@Configuration
@Data
public class ExternalServicesConfig {
    private ReniecProvidersConfig reniec;
    private SunatProvidersConfig sunat;
    private ForeignPersonProvidersConfig foreignPerson;
}
