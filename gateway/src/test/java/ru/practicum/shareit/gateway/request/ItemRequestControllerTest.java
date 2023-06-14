package ru.practicum.shareit.gateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.gateway.util.constant.Header;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@MockBean(ItemRequestClient.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should throw exception when description is blank")
    void addWhenDescriptionBlankThenThrow() throws Exception {
        mockMvc.perform(post("/requests")
                        .header(Header.USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ItemRequestRequestDto("")))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}