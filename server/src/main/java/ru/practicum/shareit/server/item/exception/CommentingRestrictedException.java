package ru.practicum.shareit.server.item.exception;

public class CommentingRestrictedException extends RuntimeException {
    public CommentingRestrictedException(String message) {
        super(message);
    }
}
