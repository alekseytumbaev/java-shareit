package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private long nextId;
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    @Override
    public User add(User user) throws EmailAlreadyExistsException {
        if (emailIsNotUnique(user)) {
            throw new EmailAlreadyExistsException(
                    String.format("Cannot add user, because email=%s already exists", user.getEmail()));
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public User update(User user) throws UserNotFoundException, EmailAlreadyExistsException {
        User presentedUser = users.get(user.getId());
        if (presentedUser == null) {
            throw new UserNotFoundException(String.format("User with id=%d not found", user.getId()));
        }
        if (emailIsNotUnique(user)) {
            throw new EmailAlreadyExistsException(
                    String.format("Cannot update user, because email=%s already exists", user.getEmail()));
        }
        emails.remove(presentedUser.getEmail());
        emails.add(user.getEmail());
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
        User user = users.get(id);
        if (user == null) {
            return;
        }
        emails.remove(user.getEmail());
        users.remove(id);
    }

    @Override
    public boolean existsById(long id) {
        return users.containsKey(id);
    }

    private boolean emailIsNotUnique(User user) {
        User presentedUser = users.get(user.getId());
        if (presentedUser == null || !presentedUser.getEmail().equals(user.getEmail())) {
            return emails.contains(user.getEmail());
        } else {
            return false;
        }
    }

    private long getNextId() {
        return ++nextId;
    }
}
