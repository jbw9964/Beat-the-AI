package org.app.util;

import jakarta.validation.*;
import java.util.*;

public class CustomEnumValidator implements ConstraintValidator<ValidEnum, Enum<?>> {

    private ValidEnum validEnum;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        validEnum = constraintAnnotation;
    }

    @Override
    public boolean isValid(Enum value, ConstraintValidatorContext context) {
        Object[] enumValues = this.validEnum.target().getEnumConstants();

        return enumValues != null &&
               Arrays.asList(enumValues).contains(value);
    }
}
