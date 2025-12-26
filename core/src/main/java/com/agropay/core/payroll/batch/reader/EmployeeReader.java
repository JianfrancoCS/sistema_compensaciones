package com.agropay.core.payroll.batch.reader;

import com.agropay.core.organization.domain.EmployeeEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Map;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class EmployeeReader {

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaPagingItemReader<EmployeeEntity> employeeItemReader(
        @Value("#{jobExecutionContext['periodStart']}") String periodStartStr,
        @Value("#{jobExecutionContext['periodEnd']}") String periodEndStr,
        @Value("#{jobExecutionContext['subsidiaryId']}") Short subsidiaryId
    ) {
        LocalDate periodStart = LocalDate.parse(periodStartStr);
        LocalDate periodEnd = LocalDate.parse(periodEndStr);

        log.info("Configuring EmployeeReader for period: {} to {}, subsidiaryId: {}", periodStart, periodEnd, subsidiaryId);

        String queryString = "SELECT DISTINCT e FROM EmployeeEntity e " +
                "WHERE EXISTS ( " +
                "  SELECT 1 FROM com.agropay.core.assignment.domain.TareoEmployeeEntity te " +
                "  JOIN te.tareo t " +
                "  WHERE te.employee.personDocumentNumber = e.personDocumentNumber " +
                "  AND CAST(t.createdAt AS date) BETWEEN :periodStart AND :periodEnd " +
                "  AND t.subsidiary.id = :subsidiaryId " +
                "  AND te.deletedAt IS NULL " +
                "  AND t.deletedAt IS NULL " +
                ") " +
                "AND e.deletedAt IS NULL " +
                "ORDER BY e.personDocumentNumber ASC";
        
        log.info("EmployeeReader query: {}", queryString);
        log.info("EmployeeReader parameters: periodStart={}, periodEnd={}, subsidiaryId={}", periodStart, periodEnd, subsidiaryId);

        log.info("EmployeeReader: EntityManagerFactory disponible: {}", 
            entityManagerFactory != null ? "SÍ" : "NO");
        
        if (entityManagerFactory == null) {
            throw new IllegalStateException("EntityManagerFactory no puede ser null para JpaPagingItemReader");
        }
        
        JpaPagingItemReader<EmployeeEntity> reader = new JpaPagingItemReaderBuilder<EmployeeEntity>()
            .name("employeeItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString(queryString)
            .parameterValues(Map.of(
                "periodStart", periodStart,
                "periodEnd", periodEnd,
                "subsidiaryId", subsidiaryId
            ))
            .pageSize(10) 
            .saveState(true) 
            .build();
        
        log.info("✅ EmployeeReader configurado correctamente. PageSize: 10, SaveState: true");
        
        return reader;
    }
}
