package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.model.UserDto;

import javax.transaction.Transactional;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class UserServiceWithDatabaseTest {
    private final UserService userService;

    @Test
    @DisplayName("Should add and get user")
    void addAndGet() {
        UserDto userDto = userService.add(new UserDto(0, "name", "email@mail.com"));

        UserDto retrievedUser = userService.getDtoById(userDto.getId());
        assertEquals(userDto.getId(), retrievedUser.getId());
        assertEquals(userDto.getName(), retrievedUser.getName());
        assertEquals(userDto.getEmail(), retrievedUser.getEmail());
    }

    @Test
    @DisplayName("Should update user replacing null fields with previous values")
    void update() {
        UserDto userDto = userService.add(new UserDto(0, "name", "email@mail.com"));
        userService.update(new UserDto(userDto.getId(), "new name", null));

        UserDto updatedUser = userService.getDtoById(userDto.getId());
        assertEquals(userDto.getId(), updatedUser.getId());
        assertEquals("new name", updatedUser.getName());
        assertEquals(userDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    @DisplayName("Should get all users")
    void getAll() {
        UserDto userDto1 = userService.add(new UserDto(0, "name1", "email1@mail.com"));
        UserDto userDto2 = userService.add(new UserDto(0, "name2", "email2@mail.com"));

        Collection<UserDto> users = userService.getAll();
        assertEquals(2, users.size());
        Iterator<UserDto> iterator = users.iterator();
        assertEquals(userDto1.getName(), iterator.next().getName());
        assertEquals(userDto2.getName(), iterator.next().getName());
    }
}
