package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponseDto;
import ru.practicum.shareit.util.constant.Header;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    @DisplayName("Should add itemRequest")
    public void add() throws Exception {
        long authorId = 1;
        ItemRequestRequestDto itemRequestDto = new ItemRequestRequestDto("Test description");

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(1L, "Test description", LocalDateTime.now(), null);

        when(itemRequestService.add(itemRequestDto, authorId)).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(Header.USER_ID_HEADER, authorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) responseDto.getId())))
                .andExpect(jsonPath("$.description", is(responseDto.getDescription())))
                .andExpect(jsonPath("$.items", nullValue()));
    }

    @Test
    @DisplayName("Should get all itemRequests by authorId sorted by created desc")
    public void getAllByAuthorIdSortedByCreatedDesc() throws Exception {
        long authorId = 1L;

        LocalDateTime now = LocalDateTime.now();

        ItemRequestResponseDto responseDto1 = new ItemRequestResponseDto();
        responseDto1.setId(1L);
        responseDto1.setDescription("Test description 1");
        responseDto1.setCreated(now);
        responseDto1.setItems(Collections.emptyList());

        ItemRequestResponseDto responseDto2 = new ItemRequestResponseDto();
        responseDto2.setId(2L);
        responseDto2.setDescription("Test description 2");
        responseDto2.setCreated(now);
        responseDto2.setItems(Collections.emptyList());

        when(itemRequestService.getAllByAuthorIdSortedByCreatedDesc(authorId))
                .thenReturn(Arrays.asList(responseDto1, responseDto2));

        mockMvc.perform(get("/requests")
                        .header(Header.USER_ID_HEADER, authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is((int) responseDto1.getId())))
                .andExpect(jsonPath("$[0].description", is(responseDto1.getDescription())))
                .andExpect(jsonPath("$[0].items", hasSize(0)))
                .andExpect(jsonPath("$[1].id", is((int) responseDto2.getId())))
                .andExpect(jsonPath("$[1].description", is(responseDto2.getDescription())))
                .andExpect(jsonPath("$[1].created", is(responseDto2.getCreated().toString())))
                .andExpect(jsonPath("$[1].items", hasSize(0)));
    }

    @Test
    @DisplayName("Should get itemRequest by id")
    public void getById() throws Exception {
        long userId = 1L;
        long requestId = 1L;

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(requestId);
        responseDto.setDescription("Test description");
        responseDto.setCreated(LocalDateTime.now());
        responseDto.setItems(Collections.emptyList());

        when(itemRequestService.getById(userId, requestId)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(Header.USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) responseDto.getId())))
                .andExpect(jsonPath("$.description", is(responseDto.getDescription())))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    @DisplayName("Should throw exception when item request not found")
    public void getByIdNotFound() throws Exception {
        long userId = 1L;
        long requestId = 1L;

        when(itemRequestService.getById(requestId, userId)).thenThrow(new ItemRequestNotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", requestId)
                        .header(Header.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get all itemRequests except author's, sorted by created desc")
    public void getAllExceptAuthor() throws Exception {
        long authorId = 1L;
        int from = 0;
        int size = 10;

        LocalDateTime now = LocalDateTime.now();

        ItemRequestResponseDto responseDto1 = new ItemRequestResponseDto();
        responseDto1.setId(1L);
        responseDto1.setDescription("Test description 1");
        responseDto1.setCreated(now);
        responseDto1.setItems(Collections.emptyList());
        ItemRequestResponseDto responseDto2 = new ItemRequestResponseDto();
        responseDto2.setId(2L);
        responseDto2.setDescription("Test description 2");
        responseDto2.setCreated(now);
        responseDto2.setItems(Collections.emptyList());

        when(itemRequestService.getAllExceptAuthorIdSortedByCreatedDesc(from, size, authorId))
                .thenReturn(Arrays.asList(responseDto1, responseDto2));

        mockMvc.perform(get("/requests/all")
                        .header(Header.USER_ID_HEADER, authorId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is((int) responseDto1.getId())))
                .andExpect(jsonPath("$[0].description", is(responseDto1.getDescription())))
                .andExpect(jsonPath("$[0].items", hasSize(0)))
                .andExpect(jsonPath("$[1].id", is((int) responseDto2.getId())))
                .andExpect(jsonPath("$[1].description", is(responseDto2.getDescription())))
                .andExpect(jsonPath("$[1].items", hasSize(0)));
    }
}
