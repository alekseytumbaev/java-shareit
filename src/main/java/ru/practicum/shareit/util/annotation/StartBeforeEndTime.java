package ru.practicum.shareit.util.annotation;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;
import java.time.LocalDateTime;

/**
 * Supports only {@link LocalDateTime} of start and end fields.
 * Null values of start and end fields are considered valid
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartBeforeEndTime.Validator.class)
@Documented
public @interface StartBeforeEndTime {
    String startField();
    String endField();

    String message() default "start time must be before end time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<StartBeforeEndTime, Object> {
        private String startField;
        private String endField;

        @Override
        public void initialize(StartBeforeEndTime constraintAnnotation) {
            startField = constraintAnnotation.startField();
            endField = constraintAnnotation.endField();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }

            BeanWrapper beanWrapper = new BeanWrapperImpl(value);
            Object start = beanWrapper.getPropertyValue(startField);
            Object end = beanWrapper.getPropertyValue(endField);

            if (start == null || end == null) {
                return true;
            }

            LocalDateTime startTime = start instanceof LocalDateTime ? (LocalDateTime) start : null;
            LocalDateTime endTime = end instanceof LocalDateTime ? (LocalDateTime) end : null;

            if (startTime == null || endTime == null) {
                throw new IllegalArgumentException(
                        String.format("'%s' and '%s' fields must be of type LocalDateTime", startField, endField));
            }

            return startTime.isBefore(endTime);
        }
    }
}
