package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {
    private long nextId;
    protected final Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        if (emailIsNotUnique(user))
            throw new EmailAlreadyExistsException(
                    String.format("Cannot add user, because email=%s already exists", user.getEmail()));
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) throws UserNotFoundException, EmailAlreadyExistsException {
        if (!users.containsKey(user.getId()))
            throw new UserNotFoundException(String.format("User with id=%d not found", user.getId()));
        if (emailIsNotUnique(user))
            throw new EmailAlreadyExistsException(
                    String.format("Cannot update user, because email=%s already exists", user.getEmail()));
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    @Override
    public boolean existsById(long id) {
        return users.containsKey(id);
    }

    private boolean emailIsNotUnique(User user) {
        for (User presentedUser : users.values())
            if (user.getId() != presentedUser.getId() && user.getEmail().equals(presentedUser.getEmail()))
                return true;
        return false;
    }

    private long getNextId() {
        return ++nextId;
    }
}
