package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.error.ErrorResponse;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNullFieldsException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserErrorHandler {

    @ExceptionHandler(UserNullFieldsException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onUserNullFieldsException(final UserNullFieldsException e) {
        String message = "User fields must not be null";
        log.warn(message, e);
        return ErrorResponse.builder().message(message).build();
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(CONFLICT)
    public ErrorResponse onEmailAlreadyExistsException(final EmailAlreadyExistsException e) {
        String message = "Email already exists";
        log.warn(message, e);
        return ErrorResponse.builder().message(message).build();
    }
}