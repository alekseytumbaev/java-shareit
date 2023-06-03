package ru.practicum.shareit.item.model;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.dto.SimpleBookingDtoResponse;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsDtoResponse;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

@Component
public class ItemMapper {
    private final UserService userService;

    public ItemMapper(UserService userService) {
        this.userService = userService;
    }

    public Item toItem(ItemDto itemDto) {
        User owner = userService.getById(itemDto.getOwnerId());
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                itemDto.getRequest());
    }

    public ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                item.getRequest());
    }

    public ItemWithBookingsDtoResponse toItemWithBookingsDtoResponse(Item item, SimpleBookingDtoResponse lastBooking,
                                                                SimpleBookingDtoResponse nextBooking) {
        return new ItemWithBookingsDtoResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                lastBooking,
                nextBooking,
                item.getRequest());
    }
}
