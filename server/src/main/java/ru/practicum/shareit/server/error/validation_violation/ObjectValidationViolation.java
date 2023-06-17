package ru.practicum.shareit.server.error.validation_violation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ObjectValidationViolation {
    private final String message;
}
