package com.agropay.core.engine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {
    private final Locale defaultLocale = new Locale("es");

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(
                Arrays.asList(
                        defaultLocale,
                        new Locale("en")
                )
        );
        resolver.setDefaultLocale(defaultLocale);
        return resolver;
    }
}
