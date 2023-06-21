package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.server.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.request.model.dto.ItemRequestRequestDto;
import ru.practicum.shareit.server.request.model.dto.ItemRequestResponseDto;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    @Mock
    private ItemRequestRepository itemRequestRepo;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepo;

    @InjectMocks
    private ItemRequestService itemRequestService;


    @Test
    @DisplayName("Should convert an empty list of ItemRequests to an empty list of ItemRequestResponseDtos")
    void convertToResponseDtoListWithEmptyList() {
        List<ItemRequest> itemRequests = List.of();

        when(itemRepo.findAllByRequest_IdAsMap(any())).thenReturn(Map.of());

        List<ItemRequestResponseDto> actualResponseDtos = ReflectionTestUtils.invokeMethod(
                itemRequestService, "convertToResponseDtoList", itemRequests);

        assertEquals(0, actualResponseDtos.size());

        verify(itemRepo, times(1)).findAllByRequest_IdAsMap(List.of());
    }

    @Test
    @DisplayName("Should convert a list of ItemRequests with no responses to a list of ItemRequestResponseDtos with empty response lists")
    void convertToResponseDtoListWithNoResponses() {
        ItemRequest itemRequest1 = new ItemRequest(1L, "description1", LocalDateTime.now(), new User(1L, "user1", "user1@example.com"));
        ItemRequest itemRequest2 = new ItemRequest(2L, "description2", LocalDateTime.now(), new User(2L, "user2", "user2@example.com"));
        Map<Long, List<Item>> itemRequests = new HashMap<>();
        itemRequests.put(1L, List.of());
        itemRequests.put(2L, List.of());

        when(itemRepo.findAllByRequest_IdAsMap(any())).thenReturn(itemRequests);

        List<ItemRequestResponseDto> actualResponseDtos = ReflectionTestUtils.invokeMethod(
                itemRequestService, "convertToResponseDtoList", List.of(itemRequest1, itemRequest2));

        assertEquals(2, actualResponseDtos.size());
        assertItemRequestEntityAndResponseDtoEquals(itemRequest1, List.of(), actualResponseDtos.get(0));
        assertItemRequestEntityAndResponseDtoEquals(itemRequest2, List.of(), actualResponseDtos.get(1));

        verify(itemRepo, times(1)).findAllByRequest_IdAsMap(List.of(1L, 2L));
    }

    @Test
    @DisplayName("Should convert a list of ItemRequests with responses to a list of ItemRequestResponseDtos with corresponding response lists")
    void convertToResponseDtoListWithResponses() {
        User author = new User(1L, "John Doe", "john.doe@example.com");
        ItemRequest itemRequest1 = new ItemRequest(1L, "Request 1", LocalDateTime.now(), author);
        ItemRequest itemRequest2 = new ItemRequest(2L, "Request 2", LocalDateTime.now(), author);
        ItemRequest itemRequest3 = new ItemRequest(3L, "Request 3", LocalDateTime.now(), author);
        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2, itemRequest3);

        Item item1 = new Item(1L, "Item 1", "Item 1 description", true, author, itemRequest1);
        Item item2 = new Item(2L, "Item 2", "Item 2 description", false, author, itemRequest1);
        Item item3 = new Item(3L, "Item 3", "Item 3 description", true, author, itemRequest2);
        Item item4 = new Item(4L, "Item 4", "Item 4 description", false, author, itemRequest2);
        Item item5 = new Item(5L, "Item 5", "Item 5 description", true, author, itemRequest3);
        Item item6 = new Item(6L, "Item 6", "Item 6 description", false, author, itemRequest3);
        List<Item> items1 = List.of(item1, item2);
        List<Item> items2 = List.of(item3, item4);
        List<Item> items3 = List.of(item5, item6);
        List<Long> requestIds = List.of(itemRequest1.getId(), itemRequest2.getId(), itemRequest3.getId());
        when(itemRepo.findAllByRequest_IdAsMap(requestIds)).thenReturn(
                Map.of(
                        itemRequest1.getId(), items1,
                        itemRequest2.getId(), items2,
                        itemRequest3.getId(), items3
                )
        );

        List<ItemRequestResponseDto> result = ReflectionTestUtils.invokeMethod(
                itemRequestService, "convertToResponseDtoList", itemRequests
        );

        assertEquals(3, result.size());

        assertItemRequestEntityAndResponseDtoEquals(itemRequest1, items1, result.get(0));
        assertItemRequestEntityAndResponseDtoEquals(itemRequest2, items2, result.get(1));
        assertItemRequestEntityAndResponseDtoEquals(itemRequest3, items3, result.get(2));
    }

    private void assertItemRequestEntityAndResponseDtoEquals(ItemRequest itemRequest, List<Item> responses, ItemRequestResponseDto itemRequestResponseDto) {
        assertEquals(itemRequest.getId(), itemRequestResponseDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestResponseDto.getDescription());
        assertEquals(itemRequest.getCreated(), itemRequestResponseDto.getCreated());
        assertEquals(responses.size(), itemRequestResponseDto.getItems().size());
        for (int i = 0; i < responses.size(); i++) {
            assertEquals(responses.get(i).getId(), itemRequestResponseDto.getItems().get(i).getId());
            assertEquals(responses.get(i).getName(), itemRequestResponseDto.getItems().get(i).getName());
            assertEquals(responses.get(i).getDescription(), itemRequestResponseDto.getItems().get(i).getDescription());
            assertEquals(responses.get(i).getAvailable(), itemRequestResponseDto.getItems().get(i).getAvailable());
            assertEquals(responses.get(i).getOwner().getId(), itemRequestResponseDto.getItems().get(i).getOwnerId());
        }
    }

    @Test
    @DisplayName("Should return all item requests except those created by the author, sorted by creation date in descending order")
    void getAllExceptAuthorIdSortedByCreatedDesc() {
        User author = new User(1L, "John Doe", "john.doe@example.com");
        long authorId = author.getId();
        int from = 0;
        int size = 10;

        ItemRequest itemRequest1 = new ItemRequest(1L, "Request 1", LocalDateTime.now(), author);
        ItemRequest itemRequest2 = new ItemRequest(2L, "Request 2", LocalDateTime.now().minusDays(1), author);
        ItemRequest itemRequest3 = new ItemRequest(3L, "Request 3", LocalDateTime.now().minusDays(2), author);

        Page<ItemRequest> itemRequests = new PageImpl<>(List.of(itemRequest1, itemRequest2, itemRequest3));

        when(userService.getById(authorId)).thenReturn(author);
        when(itemRequestRepo.findAllExceptAuthor_Id(any(), eq(authorId))).thenReturn(itemRequests);

        Collection<ItemRequestResponseDto> itemRequestResponseDtos = itemRequestService.getAllExceptAuthorIdSortedByCreatedDesc(from, size, authorId);

        assertEquals(3, itemRequestResponseDtos.size());
        assertEquals(itemRequest1.getCreated(), itemRequestResponseDtos.iterator().next().getCreated());
        verify(userService, times(1)).getById(authorId);
        verify(itemRequestRepo, times(1)).findAllExceptAuthor_Id(any(), eq(authorId));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when the user ID is not found")
    void getByIdWhenUserIdNotFoundThenThrowUserNotFoundException() {
        long userId = 1L;
        long requestId = 2L;

        when(userService.existsById(userId)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> itemRequestService.getById(userId, requestId));

        verify(userService, times(1)).existsById(userId);
        verify(itemRequestRepo, times(0)).findById(requestId);
    }

    @Test
    @DisplayName("Should throw ItemRequestNotFoundException when the request ID is not found")
    void getByIdWhenRequestIdNotFoundThenThrowItemRequestNotFoundException() {
        long userId = 1L;
        long requestId = 2L;
        when(userService.existsById(userId)).thenReturn(true);
        when(itemRequestRepo.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(ItemRequestNotFoundException.class, () -> itemRequestService.getById(userId, requestId));

        verify(userService, times(1)).existsById(userId);
        verify(itemRequestRepo, times(1)).findById(requestId);
    }

    @Test
    @DisplayName("Should return the item request when the user and request IDs are valid")
    void getByIdWhenUserAndRequestIdsAreValid() {
        long userId = 1L;
        long requestId = 2L;
        User user = new User(userId, "John Doe", "john.doe@example.com");
        LocalDateTime now = LocalDateTime.now();
        ItemRequest itemRequest = new ItemRequest(requestId, "Test request", now, user);
        when(userService.existsById(userId)).thenReturn(true);
        when(itemRequestRepo.findById(requestId)).thenReturn(Optional.of(itemRequest));

        ItemRequestResponseDto result = itemRequestService.getById(userId, requestId);

        assertEquals(requestId, result.getId());
        assertEquals("Test request", result.getDescription());
        assertEquals(now, result.getCreated());
        assertEquals(0, result.getItems().size());

        verify(userService, times(1)).existsById(userId);
        verify(itemRequestRepo, times(1)).findById(requestId);
    }

    @Test
    @DisplayName("Should return all item requests by author ID sorted by created date in descending order")
    void getAllByAuthorIdSortedByCreatedDesc() {
        User author = new User(1L, "John Doe", "john.doe@example.com");
        ItemRequest itemRequest1 = new ItemRequest(1L, "Request 1", LocalDateTime.now(), author);
        ItemRequest itemRequest2 = new ItemRequest(2L, "Request 2", LocalDateTime.now().minusDays(1), author);
        ItemRequest itemRequest3 = new ItemRequest(3L, "Request 3", LocalDateTime.now().minusDays(2), author);

        when(userService.getById(author.getId())).thenReturn(author);
        when(itemRequestRepo.findAllByAuthor_IdOrderByCreatedDesc(author.getId()))
                .thenReturn(List.of(itemRequest1, itemRequest2, itemRequest3));

        Collection<ItemRequestResponseDto> itemRequestResponseDtos = itemRequestService.getAllByAuthorIdSortedByCreatedDesc(author.getId());

        assertEquals(3, itemRequestResponseDtos.size());
        assertEquals(itemRequest1.getId(), itemRequestResponseDtos.iterator().next().getId());
        verify(userService, times(1)).getById(author.getId());
        verify(itemRequestRepo, times(1)).findAllByAuthor_IdOrderByCreatedDesc(author.getId());
    }

    @Test
    @DisplayName("Should throw a UserNotFoundException when the user is not found")
    void addItemRequestWhenUserNotFoundThenThrowException() {
        ItemRequestRequestDto itemRequestDto = new ItemRequestRequestDto("test description");
        long userId = 1L;
        when(userService.getById(userId)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> itemRequestService.add(itemRequestDto, userId));

        verify(userService, times(1)).getById(userId);
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(itemRequestRepo);
        verifyNoInteractions(itemRepo);
    }

    @Test
    @DisplayName("Should add a new item request and return the response DTO when the user is found")
    void addItemRequestWhenUserIsFound() {
        long userId = 1L;
        ItemRequestRequestDto itemRequestDto = new ItemRequestRequestDto("Test item request");
        User author = new User(userId, "Test User", "testuser@example.com");
        ItemRequest itemRequest = new ItemRequest(1L, itemRequestDto.getDescription(), LocalDateTime.now(), author);
        ItemRequestResponseDto expectedResponseDto = new ItemRequestResponseDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                null
        );

        when(userService.getById(userId)).thenReturn(author);
        when(itemRequestRepo.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestResponseDto actualResponseDto = itemRequestService.add(itemRequestDto, userId);

        assertEquals(expectedResponseDto, actualResponseDto);
        verify(userService, times(1)).getById(userId);
        verify(itemRequestRepo, times(1)).save(any(ItemRequest.class));
    }
}