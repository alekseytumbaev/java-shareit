package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(User user) throws EmailAlreadyExistsException {
        return userStorage.add(user);
    }

    public User update(User user) throws UserNotFoundException {
        Optional<User> userOpt = userStorage.getById(user.getId());
        if (userOpt.isEmpty())
            throw new UserNotFoundException(String.format("User with id=%d not found", user.getId()));

        User presentedUser = userOpt.get();
        if (user.getName() == null)
            user.setName(presentedUser.getName());
        if (user.getEmail() == null)
            user.setEmail(presentedUser.getEmail());

        return userStorage.update(user);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(long id) throws UserNotFoundException {
        Optional<User> userOpt = userStorage.getById(id);
        if (userOpt.isEmpty())
            throw new UserNotFoundException(String.format("User with id=%d not found", id));

        return userOpt.get();
    }

    public void delete(long id) {
        userStorage.delete(id);
    }
}
