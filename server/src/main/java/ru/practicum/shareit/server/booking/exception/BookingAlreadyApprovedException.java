package ru.practicum.shareit.server.booking.exception;

public class BookingAlreadyApprovedException extends RuntimeException {
    public BookingAlreadyApprovedException(String message) {
        super(message);
    }
}
