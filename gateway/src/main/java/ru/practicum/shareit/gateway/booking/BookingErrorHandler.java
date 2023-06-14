package ru.practicum.shareit.gateway.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.gateway.booking.exception.*;
import ru.practicum.shareit.gateway.error.ErrorResponse;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BookingErrorHandler {

    @ExceptionHandler(UnknownBookingStateException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onUnknownBookingState(final UnknownBookingStateException e) {
        String message = "Unknown state: " + e.getBookingState();
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }
}
