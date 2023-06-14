package ru.practicum.shareit.server.request;

import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.item.model.dto.ItemDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.request.model.dto.ItemRequestRequestDto;
import ru.practicum.shareit.server.request.model.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestRequestDto itemRequestDto, long id,
                                            LocalDateTime created, User author) {
        return new ItemRequest(
                id,
                itemRequestDto.getDescription(),
                created,
                author
        );
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest, List<ItemDto> items) {
        return new ItemRequestResponseDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items
        );
    }
}
