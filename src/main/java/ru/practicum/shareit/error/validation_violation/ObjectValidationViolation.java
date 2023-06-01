package ru.practicum.shareit.error.validation_violation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ObjectValidationViolation {
    private final String name;
}
