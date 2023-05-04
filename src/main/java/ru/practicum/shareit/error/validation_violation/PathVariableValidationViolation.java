package ru.practicum.shareit.error.validation_violation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PathVariableValidationViolation {
    private final String pathVariable;
    private final String message;
}
