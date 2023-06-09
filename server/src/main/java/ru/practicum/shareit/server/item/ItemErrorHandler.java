package ru.practicum.shareit.server.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.server.error.ErrorResponse;
import ru.practicum.shareit.server.item.exception.CommentingRestrictedException;
import ru.practicum.shareit.server.item.exception.ItemNotFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ItemErrorHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse onItemNotFoundException(final ItemNotFoundException e) {
        String message = "Item not found";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(CommentingRestrictedException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse commentingRestrictedException(final CommentingRestrictedException e) {
        String message = "Commenting restricted: " + e.getMessage();
        log.warn("Commenting restricted", e);
        return ErrorResponse.builder().error(message).build();
    }
}
