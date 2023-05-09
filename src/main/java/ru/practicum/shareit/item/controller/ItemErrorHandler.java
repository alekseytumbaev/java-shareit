package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.error.ErrorResponse;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.ItemNullFieldsException;
import ru.practicum.shareit.item.exception.WrongOwnerIdException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ItemErrorHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse onItemNotFoundException(final ItemNotFoundException e) {
        String message = "Item not found";
        log.warn(message, e);
        return ErrorResponse.builder().message(message).build();
    }

    @ExceptionHandler(ItemNullFieldsException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onItemNullFieldsException(final ItemNullFieldsException e) {
        String message = "Name, available and description must not be null";
        log.warn(message, e);
        return ErrorResponse.builder().message(message).build();
    }

    @ExceptionHandler(WrongOwnerIdException.class)
    @ResponseStatus(FORBIDDEN)
    public ErrorResponse onWrongOwnerIdException(final WrongOwnerIdException e) {
        String message = "This item does not belong to this user";
        log.warn(message, e);
        return ErrorResponse.builder().message(message).build();
    }
}
