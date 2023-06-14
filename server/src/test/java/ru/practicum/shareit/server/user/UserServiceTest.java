package ru.practicum.shareit.server.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.server.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.model.UserDto;
import ru.practicum.shareit.server.user.model.UserMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should get user by id")
    public void getDtoById() throws UserNotFoundException {
        User user = new User(1L, "John Doe", "johndoe@example.com");

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        UserService userService = new UserService(userRepo);

        UserDto userDto = userService.getDtoById(1L);

        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException")
    public void getDtoByIdUserNotFoundThrowException() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getDtoById(1L));
    }

    @Test
    @DisplayName("Should get all users")
    public void getAll() {
        List<User> userList = new ArrayList<>();
        userList.add(new User(1L, "John Doe", "johndoe@example.com"));
        userList.add(new User(2L, "Jane Doe", "janedoe@example.com"));

        when(userRepo.findAll()).thenReturn(userList);

        UserService userService = new UserService(userRepo);

        Collection<UserDto> userDtoList = userService.getAll();

        assertEquals(userList.size(), userDtoList.size());

        for (User user : userList) {
            UserDto userDto = userDtoList.stream().filter(dto -> dto.getId() == user.getId()).findFirst().orElse(null);
            assertNotNull(userDto);
            assertEquals(user.getName(), userDto.getName());
            assertEquals(user.getEmail(), userDto.getEmail());
        }
    }

    @Test
    @DisplayName("Should update user")
    public void update() {
        UserDto updatedUserDto = new UserDto(1L, "Jane Doe", "johndoe@example.com");

        User existingUser = new User(1L, "John Doe", "johndoe@example.com");

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepo.save(any(User.class))).thenReturn(UserMapper.toUser(updatedUserDto));

        UserService userService = new UserService(userRepo);

        UserDto updatedUser = userService.update(updatedUserDto);

        assertEquals(updatedUserDto.getName(), updatedUser.getName());
        assertEquals(existingUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    public void updateWhenNotFoundThenThrowException() {
        UserDto updatedUserDto = new UserDto(1L, "Jane Doe", "janedoe@example.com");
        when(userRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.update(updatedUserDto));
    }

    @Test
    @DisplayName("Should add user")
    public void add() {
        UserDto userDto = new UserDto(0, "John Doe", "johndoe@example.com");

        User user = new User();
        user.setId(1L);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        when(userRepo.save(any(User.class))).thenReturn(user);

        UserDto addedUserDto = userService.add(userDto);

        assertNotEquals(0, addedUserDto.getId());
        assertEquals(userDto.getName(), addedUserDto.getName());
        assertEquals(userDto.getEmail(), addedUserDto.getEmail());
    }
}