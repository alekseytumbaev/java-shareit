package ru.practicum.shareit.server.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.user.model.UserDto;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto add(@RequestBody UserDto userDto) {
        UserDto addedUser = userService.add(userDto);
        log.info("User with id={} was added", addedUser.getId());
        return addedUser;
    }

    @GetMapping
    public Collection<UserDto> getAll() {
        Collection<UserDto> users = userService.getAll();
        log.info("All users retrieved");
        return users;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        UserDto user = userService.getDtoById(id);
        log.info("User with id={} retrieved", id);
        return user;
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody UserDto userDto, @PathVariable long id) {
        userDto.setId(id);
        UserDto updatedUser = userService.update(userDto);
        log.info("User with id={} was updated", updatedUser.getId());
        return userDto;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        userService.delete(id);
        log.info("User with id={} was deleted", id);
    }
}
