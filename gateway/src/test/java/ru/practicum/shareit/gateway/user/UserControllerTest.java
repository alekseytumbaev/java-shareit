package ru.practicum.shareit.gateway.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.user.dto.UserDto;
import ru.practicum.shareit.gateway.util.constant.Header;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

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
    @DisplayName("Should throw exception when creating with null fields")
    public void addWhenNullFieldsThenThrow() throws Exception {
        UserDto userDto = new UserDto(1L, null, null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/users")
                        .header(Header.USER_ID_HEADER, 1)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Shouldn't throw exception when updating with null fields")
    public void updateWhenNullFields() throws Exception {
        long userId = 1;
        UserDto userDto = new UserDto(userId, null, null);
        when(userClient.update(userDto)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/{userId}", userId)
                        .header(Header.USER_ID_HEADER, userId)
                        .content(new ObjectMapper().writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}