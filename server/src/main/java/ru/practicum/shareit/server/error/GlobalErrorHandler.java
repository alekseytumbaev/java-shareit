package ru.practicum.shareit.server.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.server.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.server.error.global_exception.UserNotFoundException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse onUserNotFoundException(final UserNotFoundException e) {
        String message = "User not found";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse onUnauthorizedException(final UnauthorizedException e) {
        String message = "User doesn't have access to this operation";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse onThrowable(final Throwable e) {
        log.error("Unexpected error occurred", e);
        return ErrorResponse.builder().error("Unexpected error occurred: " + e.getMessage()).build();
    }
}
