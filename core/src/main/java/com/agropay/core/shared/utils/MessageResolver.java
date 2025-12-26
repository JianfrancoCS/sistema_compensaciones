package com.agropay.core.shared.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageResolver {
    private final MessageSource messageSource;

    public String getMessage(String messageKey, Object... args){
        try{
            Object[] params = args;
            // Si solo hay un argumento y es un Object[], usarlo directamente como array de parámetros
            if (args != null && args.length == 1 && args[0] instanceof Object[]) {
                params = (Object[]) args[0];
            }
            // Si hay múltiples argumentos, usarlos directamente
            else if (args != null && args.length > 0) {
                params = args;
            }
            // Si no hay argumentos, usar array vacío
            else {
                params = new Object[0];
            }
            return messageSource.getMessage(messageKey, params, LocaleContextHolder.getLocale());
        }catch (NoSuchMessageException ex){
            log.warn("Message key '{}' not found in properties, using fallback", messageKey);
            return messageKey;
        }
    }
}
