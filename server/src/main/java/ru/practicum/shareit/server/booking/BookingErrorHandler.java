package ru.practicum.shareit.server.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.server.booking.exception.*;
import ru.practicum.shareit.server.error.ErrorResponse;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BookingErrorHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ErrorResponse onBookingNotFoundException(final BookingNotFoundException e) {
        String message = "Booking not found";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(ItemUnavailableException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onItemUnavailableException(final ItemUnavailableException e) {
        String message = "Cannot book item, because it is not available";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(UnknownBookingStateException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onUnknownBookingState(final UnknownBookingStateException e) {
        String message = "Unknown state: " + e.getBookingState();
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(BookingAlreadyApprovedException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse onBookingAlreadyApprovedException(final BookingAlreadyApprovedException e) {
        String message = "Booking already approved";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }

    @ExceptionHandler(SameItemOwnerAndBookerIdException.class)
    @ResponseStatus(NOT_FOUND) //for some reason postman test requires not found status
    public ErrorResponse onSameItemOwnerAndBookerIdException(final SameItemOwnerAndBookerIdException e) {
        String message = "Booker and item owner cannot be the same user";
        log.warn(message, e);
        return ErrorResponse.builder().error(message).build();
    }
}
