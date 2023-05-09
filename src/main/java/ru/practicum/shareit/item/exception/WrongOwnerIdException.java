package ru.practicum.shareit.item.exception;

public class WrongOwnerIdException extends RuntimeException {
    public WrongOwnerIdException(String message) {
        super(message);
    }
}
