package ru.practicum.shareit.gateway.error;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.gateway.error.validation_violation.FieldValidationViolation;
import ru.practicum.shareit.gateway.error.validation_violation.HttpAttributeValidationViolation;
import ru.practicum.shareit.gateway.error.validation_violation.ObjectValidationViolation;

import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private String error;
    private List<ObjectValidationViolation> objectValidationViolations;
    private List<FieldValidationViolation> fieldValidationViolations;
    private List<HttpAttributeValidationViolation> httpAttributeValidationViolations;
}
