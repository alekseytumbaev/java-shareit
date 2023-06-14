package ru.practicum.shareit.server.booking.exception;

import lombok.Getter;

public class UnknownBookingStateException extends RuntimeException {
    @Getter
    private final String bookingState;

    public UnknownBookingStateException(String message, String bookingState) {
        super(message);
        this.bookingState = bookingState;
    }
}
