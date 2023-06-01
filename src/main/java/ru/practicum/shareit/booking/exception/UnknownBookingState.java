package ru.practicum.shareit.booking.exception;

import lombok.Getter;

public class UnknownBookingState extends RuntimeException {
    @Getter
    private final String bookingState;

    public UnknownBookingState(String message, String bookingState) {
        super(message);
        this.bookingState = bookingState;
    }
}
