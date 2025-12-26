package com.agropay.core.engine.config;

import com.agropay.core.shared.persistence.SoftRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.agropay",
        repositoryBaseClass = SoftRepositoryImpl.class
)
public class JpaConfig {
}
