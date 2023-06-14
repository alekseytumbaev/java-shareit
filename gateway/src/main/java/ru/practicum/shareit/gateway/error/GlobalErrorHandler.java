package ru.practicum.shareit.gateway.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.gateway.error.validation_violation.FieldValidationViolation;
import ru.practicum.shareit.gateway.error.validation_violation.HttpAttributeValidationViolation;
import ru.practicum.shareit.gateway.error.validation_violation.ObjectValidationViolation;

import javax.validation.ConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    //May be thrown when request body required, but not found
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        String message = "Http request is corrupted: " + e.getHttpInputMessage();
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    //Fields and object validation violations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        List<FieldValidationViolation> fieldViolations = new LinkedList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            FieldValidationViolation violation = new FieldValidationViolation(
                    fieldError.getField(), fieldError.getDefaultMessage());
            fieldViolations.add(violation);
        }

        List<ObjectValidationViolation> objectViolations = new LinkedList<>();
        for (ObjectError objectError : e.getBindingResult().getGlobalErrors()) {
            ObjectValidationViolation violation = new ObjectValidationViolation(objectError.getDefaultMessage());
            objectViolations.add(violation);
        }

        String message = "Validation failed";
        ErrorResponse error = ErrorResponse.builder()
                .error(message)
                .fieldValidationViolations(fieldViolations)
                .objectValidationViolations(objectViolations)
                .build();

        log.warn("{}: {}", message, error, e);
        return error;
    }

    //Http attributes (path variables, headers, request parameters) violations -----------------------------------------
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
        ErrorResponse error = ErrorResponse.builder().error(message).httpAttributeValidationViolations(violations).build();
        log.warn("{}: {}", message, error, e);
        return error;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        String message = String.format("Http attribute '%s' must be of type '%s', but was equal to '%s'",
                e.getName(), e.getRequiredType(), e.getValue());
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(MissingRequestValueException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onMissingRequestValueException(final MissingRequestValueException e) {
        String message = e.getMessage() == null ? "Some http request attribute is missing" : e.getMessage();
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }
    //------------------------------------------------------------------------------------------------------------------

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse onThrowable(final Throwable e) {
        log.error("Unexpected error occurred", e);
        return ErrorResponse.builder().error("Unexpected error occurred: " + e.getMessage()).build();
    }
}
