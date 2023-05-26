package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exception.UserNullFieldsException;

import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto add(@RequestBody @Valid UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getName() == null) {
            throw new UserNullFieldsException("Cannot add user, because email or name is null");
        }
        User addedUser = userService.add(UserMapper.toUser(userDto));
        log.info("User with id={} was added", addedUser.getId());
        return UserMapper.toUserDto(addedUser);
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        Collection<User> users = userService.getAll();
        log.info("All users retrieved");
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        User user = userService.getById(id);
        log.info("User with id={} retrieved", id);
        return UserMapper.toUserDto(user);
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody @Valid UserDto userDto, @PathVariable long id) {
        userDto.setId(id);
        User updatedUser = userService.update(UserMapper.toUser(userDto));
        log.info("User with id={} was updated", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        userService.delete(id);
        log.info("User with id={} was deleted", id);
    }
}
