package ru.practicum.shareit.error.validation_violation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpAttributeValidationViolation {
    private final String name;
    private final String message;
}
