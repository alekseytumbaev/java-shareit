package ru.practicum.shareit.server.booking.exception;

public class SameItemOwnerAndBookerIdException extends RuntimeException {
    public SameItemOwnerAndBookerIdException(String message) {
        super(message);
    }
}
