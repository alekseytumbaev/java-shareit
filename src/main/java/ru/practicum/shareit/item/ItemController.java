package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsDtoResponse;
import ru.practicum.shareit.util.constant.Header;
import ru.practicum.shareit.item.exception.ItemNullFieldsException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.ItemMapper;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper ItemMapper;

    public ItemController(ItemService itemService, ru.practicum.shareit.item.model.ItemMapper itemMapper) {
        this.itemService = itemService;
        ItemMapper = itemMapper;
    }

    @PostMapping
    public ItemDto add(@RequestBody @Valid ItemDto itemDto,
                       @RequestHeader(Header.USER_ID_HEADER) long ownerId) {
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null) {
            throw new ItemNullFieldsException("Cannot add item, because name, description or available is null");
        }

        itemDto.setOwnerId(ownerId);
        Item itemToAdd = ItemMapper.toItem(itemDto);
        Item addedItem = itemService.add(itemToAdd);
        log.info("Item with id={} was added", addedItem.getId());
        return ItemMapper.toItemDto(addedItem);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDtoResponse getById(@RequestHeader(Header.USER_ID_HEADER) long userId,
                                               @PathVariable long itemId) {
        ItemWithBookingsDtoResponse itemDto = itemService.getDtoById(itemId, userId);
        log.info("Item with id={} retrieved", itemDto.getId());
        return itemDto;
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchByNameOrDescription(@RequestParam @NotNull @Size(max = 200) String text) {
        Collection<Item> items = itemService.searchByNameOrDescription(text);
        log.info("Items with '{}' in name or description retrieved", text);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @GetMapping
    public Collection<ItemWithBookingsDtoResponse> getByOwnerId(@RequestHeader(Header.USER_ID_HEADER) long ownerId) {
        Collection<ItemWithBookingsDtoResponse> itemsDto = itemService.getAllByOwnerId(ownerId);
        log.info("Items of owner with id={} retrieved", ownerId);
        return itemsDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody @Valid ItemDto itemDto,
                          @PathVariable long itemId,
                          @RequestHeader(Header.USER_ID_HEADER) long userId) {
        itemDto.setOwnerId(userId);
        itemDto.setId(itemId);
        Item item = ItemMapper.toItem(itemDto);
        Item updatedItem = itemService.update(item);
        log.info("Item with id={} was updated", updatedItem.getId());
        return ItemMapper.toItemDto(updatedItem);
    }
}
