package ru.practicum.shareit.gateway.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.util.constant.Header;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@MockBean(ItemClient.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should throw exception when null fields")
    public void addWhenNullFieldsThenException() throws Exception {
        mockMvc.perform(post("/items")
                        .header(Header.USER_ID_HEADER, 1)
                        .content("{\"name\": null,\"description\":\"description\",\"available\":true}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should throw exception when request body is empty")
    public void addWhenRequestBodyIsEmptyThenException() throws Exception {
        mockMvc.perform(post("/items")
                        .header(Header.USER_ID_HEADER, 1)
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should throw exception when adding with no user id header")
    public void addWhenNoUserIdHeaderThenException() throws Exception {
        mockMvc.perform(post("/items")
                        .content("{\"name\":\"name\",\"description\":\"description\",\"available\":true}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should throw exception when text size > 200")
    public void searchWhenTextSizeGreaterThan200ThenException() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header(Header.USER_ID_HEADER, 1)
                        .param("text", "text".repeat(201))
                        .param("from", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}