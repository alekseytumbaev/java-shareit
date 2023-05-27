package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User add(User user) {
        return userRepo.save(user);
    }

    public User update(User user) throws UserNotFoundException {
        User presentedUser = getById(user.getId());

        //replace null fields with values from existing user
        if (user.getName() == null) {
            user.setName(presentedUser.getName());
        }
        //check email uniqueness
        if (user.getEmail() == null) {
            user.setEmail(presentedUser.getEmail());
        }
        return userRepo.save(user);
    }

    public Collection<User> getAll() {
        return userRepo.findAll();
    }

    public User getById(long id) throws UserNotFoundException {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(String.format("User with id=%d not found", id));
        }
        return userOpt.get();
    }

    public boolean existsById(long id) {
        return userRepo.existsById(id);
    }

    public void delete(long id) {
        userRepo.deleteById(id);
    }
}
