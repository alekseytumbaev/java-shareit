package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User add(User user) throws EmailAlreadyExistsException;

    User update(User user) throws UserNotFoundException, EmailAlreadyExistsException;

    Optional<User> getById(long id);

    Collection<User> getAll();

    void delete(long id);

    boolean existsById(long id);
}
