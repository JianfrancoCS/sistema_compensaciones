package com.agropay.core.shared.annotations;

import com.agropay.core.shared.utils.MessageResolver;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class SortableValidator implements ConstraintValidator<ValidSortFields, Object> {

    private Set<String> allowedFields;

    @Autowired
    private MessageResolver messageResolver;

    @Override
    public void initialize(ValidSortFields annotation) {
        this.allowedFields = Set.of(annotation.value());
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof BasePageableRequest request) {
            String sortBy = request.getSortBy();
            if (!allowedFields.contains(sortBy)) {
                context.disableDefaultConstraintViolation();
                String allowedFieldsStr = String.join(", ", allowedFields);
                String message = messageResolver.getMessage("exception.shared.invalid-sort-field", sortBy, allowedFieldsStr);
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode("sortBy")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}