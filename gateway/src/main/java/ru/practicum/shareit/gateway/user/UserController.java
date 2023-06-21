package ru.practicum.shareit.gateway.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.user.dto.UserDto;
import ru.practicum.shareit.gateway.util.constraint_group.Creation;

import javax.validation.Valid;
import javax.validation.groups.Default;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
public class UserController {

    private final UserClient userClient;

    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody @Validated({Creation.class, Default.class}) UserDto userDto) {
        log.info("Adding user with email={}", userDto.getEmail());
        ResponseEntity<Object> response = userClient.add(userDto);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Retrieving all users");
        ResponseEntity<Object> response = userClient.getAll();
        log.info("Response: status = {}, headers = {}", response.getStatusCode(), response.getHeaders());
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable long id) {
        log.info("Retrieving user with id={}", id);
        ResponseEntity<Object> response = userClient.getById(id);
        log.info("Response: {}", response);
        return response;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestBody @Valid UserDto userDto, @PathVariable long id) {
        userDto.setId(id);
        log.info("Updating user with id={}", userDto.getId());
        ResponseEntity<Object> response = userClient.update(userDto);
        log.info("Response: {}", response);
        return response;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable long id) {
        log.info("Deleting user with id={}", id);
        ResponseEntity<Object> response = userClient.delete(id);
        log.info("Response: {}", response);
        return response;
    }
}
