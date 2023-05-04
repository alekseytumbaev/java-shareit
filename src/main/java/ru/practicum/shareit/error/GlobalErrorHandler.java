package ru.practicum.shareit.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.error.validation_violation.FieldValidationViolation;
import ru.practicum.shareit.error.validation_violation.PathVariableValidationViolation;

import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    //Fields validation violations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        List<FieldValidationViolation> violations = new LinkedList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            FieldValidationViolation violation = new FieldValidationViolation(
                    fieldError.getField(), fieldError.getDefaultMessage());
            violations.add(violation);
        }
        String message = "Validation failed";
        ErrorResponse error = ErrorResponse.builder().message(message).fieldValidationViolations(violations).build();
        log.warn("{}: {}", message, error, e);
        return error;
    }

    //Path variables validation violations
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onConstraintViolationException(final ConstraintViolationException e) {
        String message = "Validation failed";
        List<PathVariableValidationViolation> violations = new LinkedList<>();
        e.getConstraintViolations().forEach(violation -> {
            PathVariableValidationViolation pathVariableViolation =
                    new PathVariableValidationViolation(violation.getPropertyPath().toString(), violation.getMessage());
            violations.add(pathVariableViolation);
        });
        ErrorResponse error = ErrorResponse.builder().message(message).pathVariableValidationViolations(violations).build();
        log.warn("{}: {}", message, error, e);
        return error;
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse onThrowable(final Throwable e) {
        log.error("Unexpected error occurred", e);
        return ErrorResponse.builder().message("Internal server error").build();
    }
}
