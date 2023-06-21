package ru.practicum.shareit.server.item.model;

import ru.practicum.shareit.server.booking.model.dto.SimpleBookingResponseDto;
import ru.practicum.shareit.server.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.server.item.model.dto.ItemDto;
import ru.practicum.shareit.server.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;

public class ItemMapper {

    public static Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request
        );
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                item.getRequest() == null ? 0 : item.getRequest().getId()
        );
    }

    public static ItemWithBookingsResponseDto toItemWithBookingsResponseDto(Item item, SimpleBookingResponseDto lastBooking,
                                                                            SimpleBookingResponseDto nextBooking,
                                                                            List<CommentResponseDto> commentResponseDtos) {
        return new ItemWithBookingsResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                lastBooking,
                nextBooking,
                commentResponseDtos
        );
    }
}
