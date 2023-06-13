package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.error.ErrorResponse;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ItemRequestErrorHandler {

    @ExceptionHandler(ItemRequestNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse onItemRequestNotFoundException(ItemRequestNotFoundException e) {
        String message = "Item request not found";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }
}
