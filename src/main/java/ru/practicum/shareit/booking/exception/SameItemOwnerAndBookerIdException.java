package ru.practicum.shareit.booking.exception;

public class SameItemOwnerAndBookerIdException extends RuntimeException {
    public SameItemOwnerAndBookerIdException(String message) {
        super(message);
    }
}
