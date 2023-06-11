package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.request.model.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.UserDto;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class ItemRequestServiceWithDatabaseTest {

    private final ItemRequestService itemRequestService;
    private final UserService userService;
    private final ItemService itemService;

    @Test
    @DisplayName("Should add a new item request and return the response DTO when the user is found")
    void addItemRequest() {
        UserDto user = userService.add(new UserDto(1L, "User1", "user1@ya.ru"));
        ItemRequestRequestDto itemRequestDto = new ItemRequestRequestDto("Test item request");
        ItemRequestResponseDto itemRequestResponseDto = itemRequestService.add(itemRequestDto, user.getId());
        assertEquals(itemRequestDto.getDescription(), itemRequestResponseDto.getDescription());
    }

    @Test
    @DisplayName("Should return all item requests by author ID sorted by created date in descending order")
    void getAllByAuthorIdSortedByCreatedDesc() {
        UserDto author = userService.add(new UserDto(0, "John Doe", "john.doe@example.com"));

        ItemRequestResponseDto itemRequestResponseDto1 = itemRequestService.add(
                new ItemRequestRequestDto("Request 1"), author.getId()
        );
        ItemRequestResponseDto itemRequestResponseDto2 = itemRequestService.add(
                new ItemRequestRequestDto("Request 2"), author.getId()
        );

        UserDto owner = userService.add(new UserDto(0, "Owner", "owner@example.com"));
        ItemDto itemDto = new ItemDto(
                0, "Test item", "Test description", true,
                owner.getId(), itemRequestResponseDto1.getId()
        );
        itemService.add(itemDto);
        itemDto = new ItemDto(0, "Test item 2", "Test description 2", true,
                owner.getId(), itemRequestResponseDto2.getId()
        );
        itemService.add(itemDto);

        Collection<ItemRequestResponseDto> itemRequestResponseDtos = itemRequestService
                .getAllByAuthorIdSortedByCreatedDesc(author.getId());

        assertEquals(2, itemRequestResponseDtos.size());
        Iterator<ItemRequestResponseDto> iterator = itemRequestResponseDtos.iterator();
        ItemRequestResponseDto itemRequestResponseDtoFromIter1 = iterator.next();
        assertEquals(1, itemRequestResponseDtoFromIter1.getItems().size());
        ItemRequestResponseDto itemRequestResponseDtoFromIter2 = iterator.next();
        assertEquals(1, itemRequestResponseDtoFromIter2.getItems().size());
    }

    @Test
    @DisplayName("Should return all item requests except those created by the author, sorted by creation date in descending order")
    void getAllExceptAuthorIdSortedByCreatedDesc() {
        int from = 0;
        int size = 10;

        UserDto author = userService.add(new UserDto(0, "John Doe", "john.doe@example.com"));
        UserDto anotherUser = userService.add(new UserDto(0, "Another User", "another.user@ya.ru"));
        itemRequestService.add(
                new ItemRequestRequestDto("Request 1"), author.getId()
        );
        ItemRequestResponseDto itemRequestResponseDto = itemRequestService.add(
                new ItemRequestRequestDto("Request 2"), anotherUser.getId()
        );

        Collection<ItemRequestResponseDto> itemRequestResponseDtos = itemRequestService
                .getAllExceptAuthorIdSortedByCreatedDesc(from, size, author.getId());

        assertEquals(1, itemRequestResponseDtos.size());
        assertEquals(itemRequestResponseDto.getDescription(), itemRequestResponseDtos.iterator().next().getDescription());
    }
}
