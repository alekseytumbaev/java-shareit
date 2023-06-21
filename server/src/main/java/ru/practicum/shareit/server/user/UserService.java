package ru.practicum.shareit.server.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.model.UserDto;
import ru.practicum.shareit.server.user.model.UserMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserDto add(UserDto userDto) {
        userDto.setId(0);
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepo.save(user));
    }

    public UserDto update(UserDto user) throws UserNotFoundException {
        User presentedUser = getById(user.getId());

        //replace null fields with values from existing user
        if (user.getName() == null) {
            user.setName(presentedUser.getName());
        }
        if (user.getEmail() == null) {
            user.setEmail(presentedUser.getEmail());
        }

        User updatedUser = userRepo.save(UserMapper.toUser(user));
        return UserMapper.toUserDto(updatedUser);
    }

    public Collection<UserDto> getAll() {
        return userRepo.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public UserDto getDtoById(long id) throws UserNotFoundException {
        return UserMapper.toUserDto(getById(id));
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
