package ru.practicum.shareit.item.model;

import ru.practicum.shareit.booking.model.dto.SimpleBookingResponseDto;
import ru.practicum.shareit.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public class ItemMapper {

    public static Item toItem(ItemDto itemDto, User owner) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                itemDto.getRequest());
    }

    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                item.getRequest());
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
                commentResponseDtos,
                item.getRequest());
    }
}
