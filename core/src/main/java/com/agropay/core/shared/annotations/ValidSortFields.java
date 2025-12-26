package com.agropay.core.shared.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SortableValidator.class)
public @interface ValidSortFields {
    String[] value();
    String message() default "{exception.shared.invalid-sort-field}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}