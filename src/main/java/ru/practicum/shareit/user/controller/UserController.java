package ru.practicum.shareit.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exception.UserNullFieldsException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;

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
    public User add(@RequestBody @Valid User user) {
        if (user.getEmail() == null || user.getName() == null)
            throw new UserNullFieldsException("Cannot add user, because email or name is null");

        User addedUser = userService.add(user);
        log.info("User with id={} was added", addedUser.getId());
        return addedUser;
    }

    @GetMapping
    public Collection<User> getAll() {
        Collection<User> users = userService.getAll();
        log.info("All users retrieved");
        return users;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable @Positive long id) {
        User user = userService.getById(id);
        log.info("User with id={} retrieved", id);
        return user;
    }

    @PatchMapping("/{id}")
    public User update(@RequestBody @Valid User user, @PathVariable @Positive long id) {
        user.setId(id);
        User updatedUser = userService.update(user);
        log.info("User with id={} was updated", updatedUser.getId());
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive long id) {
        userService.delete(id);
        log.info("User with id={} was deleted", id);
    }
}
