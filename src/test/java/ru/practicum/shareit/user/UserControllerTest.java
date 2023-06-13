package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.model.UserDto;
import ru.practicum.shareit.util.constant.Header;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Should throw exception when add user with not well-formed email")
    public void addUserWithNotWellFormedEmail() throws Exception {
        UserDto userDto = new UserDto(1L, "John Doe", "john.doe");
        mockMvc.perform(post("/users")
                        .content(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should throw exception when add user with the same email")
    public void addUserWithTheSameEmail() throws Exception {
        when(userService.add(any())).thenThrow(new DataIntegrityViolationException("User with the same email already exists"));
        UserDto userDto = new UserDto(1L, "John Doe", "john.doe@example.com");
        mockMvc.perform(post("/users")
                        .content(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should add user")
    public void add() throws Exception {
        UserDto userDto = new UserDto(1L, "John Doe", "john.doe@example.com");
        when(userService.add(userDto)).thenReturn(userDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should throw exception when null fields")
    public void addWhenNullFieldsThenThrow() throws Exception {
        UserDto userDto = new UserDto(1L, null, "john.doe@example.com");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/users")
                        .header(Header.USER_ID_HEADER, 1)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all users")
    public void testGetAll() throws Exception {
        UserDto userDto = new UserDto(1L, "John Doe", "john.doe@example.com");
        Collection<UserDto> users = Arrays.asList(userDto);
        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should get user by id")
    public void getById() throws Exception {
        long userId = 1L;
        UserDto userDto = new UserDto(userId, "John Doe", "john.doe@example.com");
        when(userService.getDtoById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should update user")
    public void update() throws Exception {
        long userId = 1L;
        UserDto userDto = new UserDto(userId, "John Doe", "john.doe@example.com");
        when(userService.update(userDto)).thenReturn(userDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mockMvc.perform(patch("/users/{userId}", userId)
                        .header(Header.USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    public void testDelete() throws Exception {
        long userId = 1L;
        mockMvc.perform(delete("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(userService).delete(userId);
    }
}
