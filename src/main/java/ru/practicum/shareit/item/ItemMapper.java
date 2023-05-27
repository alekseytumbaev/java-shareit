package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

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
}
