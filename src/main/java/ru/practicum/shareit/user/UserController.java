package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.exception.UserNullFieldsException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.user.model.UserMapper;

import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public UserDto add(@RequestBody @Valid UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getName() == null) {
            throw new UserNullFieldsException("Cannot add user, because email or name is null");
        }
        User addedUser = userService.add(userMapper.toUser(userDto));
        log.info("User with id={} was added", addedUser.getId());
        return userMapper.toUserDto(addedUser);
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        Collection<User> users = userService.getAll();
        log.info("All users retrieved");
        return users.stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        User user = userService.getById(id);
        log.info("User with id={} retrieved", id);
        return userMapper.toUserDto(user);
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody @Valid UserDto userDto, @PathVariable long id) {
        userDto.setId(id);
        User updatedUser = userService.update(userMapper.toUser(userDto));
        log.info("User with id={} was updated", updatedUser.getId());
        return userMapper.toUserDto(updatedUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        userService.delete(id);
        log.info("User with id={} was deleted", id);
    }
}
