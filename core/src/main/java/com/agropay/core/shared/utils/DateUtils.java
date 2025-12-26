package com.agropay.core.shared.utils;

import com.agropay.core.shared.exceptions.InvalidDateFormatException; // Importar la nueva excepción
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@Slf4j
public final class DateUtils {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    );

    public static LocalDate parseFlexible(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            log.debug("parseFlexible: fecha nula o vacía, retornando null");
            return null;
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ex) {
                log.trace("Formato '{}' no coincide con '{}'", formatter, dateStr);
            }
        }

        log.error("No se pudo parsear la fecha '{}' con los formatos conocidos.", dateStr);
        // Lanzar la excepción personalizada
        throw new InvalidDateFormatException("exception.shared.invalid-date-format", new Object[]{dateStr});
    }
}