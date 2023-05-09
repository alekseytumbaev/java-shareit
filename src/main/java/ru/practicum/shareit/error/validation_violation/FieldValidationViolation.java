package ru.practicum.shareit.error.validation_violation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldValidationViolation {
    private final String field;
    private final String message;
}
