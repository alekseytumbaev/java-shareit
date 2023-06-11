package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.exception.CommentingRestrictedException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.util.constant.Header;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("Should add item")
    public void add() throws Exception {
        long ownerId = 1L;
        ItemDto itemDto = new ItemDto(0, "name", "description", true, ownerId, 0);
        when(itemService.add(itemDto)).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(Header.USER_ID_HEADER, ownerId)
                        .content("{\"name\":\"name\",\"description\":\"description\",\"available\":true}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.available").value(true));
    }

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
    @DisplayName("Should throw exception when adding with no user id header")
    public void addWhenNoUserIdHeaderThenException() throws Exception {
        mockMvc.perform(post("/items")
                        .content("{\"name\":\"name\",\"description\":\"description\",\"available\":true}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should throw exception when wrong type of user id header")
    public void addWhenWrongUserIdHeader() throws Exception {
        mockMvc.perform(post("/items", 1L)
                        .header(Header.USER_ID_HEADER, "asdfsadf")
                        .content("{\"name\":\"name\",\"description\":\"description\",\"available\":true}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should throw exception when request body is empty")
    public void addWhenUnknownException() throws Exception {
        long ownerId = 1L;
        ItemDto itemDto = new ItemDto(0, "name", "description", true, ownerId, 0);
        when(itemService.add(itemDto)).thenThrow(new RuntimeException("Unknown exception"));

        mockMvc.perform(post("/items")
                        .header(Header.USER_ID_HEADER, ownerId)
                        .content("{\"name\":\"name\",\"description\":\"description\",\"available\":true}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 500 when unknown exception")
    public void addWhenRequestBodyIsEmptyThenException() throws Exception {
        mockMvc.perform(post("/items")
                        .header(Header.USER_ID_HEADER, 1)
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get item by id")
    public void getById() throws Exception {
        long userId = 1L;
        long itemId = 2L;
        ItemWithBookingsResponseDto itemDto = new ItemWithBookingsResponseDto(
                itemId, "name", "description", true, userId,
                null, null, null
        );
        when(itemService.getDtoById(itemId, userId)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(Header.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    public void getByIdWhenItemNotFoundThenException() throws Exception {
        long userId = 1L;
        long itemId = 2L;
        when(itemService.getDtoById(itemId, userId)).thenThrow(new ItemNotFoundException("Item not found"));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(Header.USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should search items by name or description")
    public void searchByNameOrDescription() throws Exception {
        long userId = 1L;
        String text = "text";
        int from = 0;
        int size = 10;
        ItemDto itemDto = new ItemDto(1L, "text", "text", true, userId, 0);
        Collection<ItemDto> items = Arrays.asList(itemDto);
        when(itemService.searchByNameOrDescription(text, from, size)).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .header(Header.USER_ID_HEADER, userId)
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("text"))
                .andExpect(jsonPath("$[0].description").value("text"))
                .andExpect(jsonPath("$[0].available").value(true));
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

    @Test
    @DisplayName("Should get all items by owner id")
    public void getAllByOwnerId() throws Exception {
        long ownerId = 1L;
        int from = 0;
        int size = 10;
        ItemWithBookingsResponseDto itemDto = new ItemWithBookingsResponseDto(
                1L, "name", "description", true, ownerId,
                null, null, null
        );
        Collection<ItemWithBookingsResponseDto> itemsDto = Arrays.asList(itemDto);
        when(itemService.getAllByOwnerId(ownerId, from, size)).thenReturn(itemsDto);

        mockMvc.perform(get("/items")
                        .header(Header.USER_ID_HEADER, ownerId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("name"))
                .andExpect(jsonPath("$[0].description").value("description"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    @DisplayName("Should update item")
    public void update() throws Exception {
        long userId = 1L;
        long itemId = 2L;
        ItemDto itemDto = new ItemDto(itemId, "name", "description", false, userId, 0);
        ItemDto updatedItem = new ItemDto(itemId, "new name", "new description", true, userId, 0);
        when(itemService.update(itemDto)).thenReturn(updatedItem);

        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(Header.USER_ID_HEADER, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("new name"))
                .andExpect(jsonPath("$.description").value("new description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("Should add comment")
    public void addComment() throws Exception {
        long authorId = 1L;
        long itemId = 2L;
        String authorName = "author name";
        String text = "text";
        CommentRequestDto commentRequestDto = new CommentRequestDto(text);
        LocalDateTime now = LocalDateTime.now();
        CommentResponseDto commentResponseDto = new CommentResponseDto(1L, text, authorName, now);
        when(itemService.addComment(commentRequestDto, itemId, authorId)).thenReturn(commentResponseDto);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(Header.USER_ID_HEADER, authorId)
                        .content(mapper.writeValueAsString(commentRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.authorName").value(authorName));
    }

    @Test
    @DisplayName("Should throw exception when commenting restricted")
    public void addCommentIdWhenCommentingRestrictedThenException() throws Exception {
        long userId = 1L;
        long itemId = 2L;
        CommentRequestDto commentRequestDto = new CommentRequestDto("text");
        when(itemService.addComment(commentRequestDto, itemId, userId)).thenThrow(
                new CommentingRestrictedException("Commenting restricted")
        );

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(Header.USER_ID_HEADER, userId)
                        .content(new ObjectMapper().writeValueAsString(commentRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}
