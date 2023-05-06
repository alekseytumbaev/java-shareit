package ru.practicum.shareit.error;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.error.validation_violation.FieldValidationViolation;
import ru.practicum.shareit.error.validation_violation.HttpAttributeValidationViolation;

import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private List<FieldValidationViolation> fieldValidationViolations;
    private List<HttpAttributeValidationViolation> httpAttributeValidationViolations;
}
