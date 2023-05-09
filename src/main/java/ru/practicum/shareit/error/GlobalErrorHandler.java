package ru.practicum.shareit.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.error.validation_violation.FieldValidationViolation;
import ru.practicum.shareit.error.validation_violation.HttpAttributeValidationViolation;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse onUserNotFoundException(final UserNotFoundException e) {
        String message = "User not found";
        log.warn(message, e);
        return ErrorResponse.builder().message(message).build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        String message = "Http request is corrupted";
        log.warn(message, e);
        return ErrorResponse.builder().message(message).build();
    }

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

    //Path variables, headers, request parameters violations
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onConstraintViolationException(final ConstraintViolationException e) {
        String message = "Validation failed";
        List<HttpAttributeValidationViolation> violations = new LinkedList<>();
        e.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String parameter = propertyPath.substring(propertyPath.lastIndexOf(".") + 1);
            HttpAttributeValidationViolation pathVariableViolation =
                    new HttpAttributeValidationViolation(parameter, violation.getMessage());
            violations.add(pathVariableViolation);
        });
        ErrorResponse error = ErrorResponse.builder().message(message).httpAttributeValidationViolations(violations).build();
        log.warn("{}: {}", message, error, e);
        return error;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onMissingRequestHeaderException(final MissingRequestHeaderException e) {
        return ErrorResponse.builder().message(String.format("'%s' request header is missing", e.getHeaderName())).build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse onThrowable(final Throwable e) {
        log.error("Unexpected error occurred", e);
        return ErrorResponse.builder().message("Internal server error").build();
    }
}
