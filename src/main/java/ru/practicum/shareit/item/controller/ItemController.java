package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.exception.ItemNullFieldsException;

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

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto add(@RequestBody @Valid ItemDto itemDto,
                       @RequestHeader("X-Sharer-User-Id") long ownerId) {
        if (itemDto.getName() == null || itemDto.getDescription() == null || itemDto.getAvailable() == null)
            throw new ItemNullFieldsException("Cannot add item, because name, description or available is null");

        itemDto.setOwnerId(ownerId);
        Item itemToAdd = ItemMapper.toItem(itemDto);
        Item addedItem = itemService.add(itemToAdd);
        log.info("Item with id={} was added", addedItem.getId());
        return ItemMapper.toItemDto(addedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable long itemId) {
        Item item = itemService.getById(itemId);
        log.info("Item with id={} retrieved", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchByNameOrDescription(@RequestParam @NotNull @Size(max = 200) String text) {
        Collection<Item> items = itemService.searchByNameOrDescription(text);
        log.info("Items with '{}' in name or description retrieved", text);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @GetMapping
    public Collection<ItemDto> getByOwnerId(@RequestHeader("X-Sharer-User-Id") long userId) {
        Collection<Item> items = itemService.getByOwnerId(userId);
        log.info("Items of owner with id={} retrieved", userId);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody @Valid ItemDto itemDto,
                          @PathVariable long itemId,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
        Item item = ItemMapper.toItem(itemDto);
        item.setId(itemId);
        item.setOwnerId(userId);
        Item updatedItem = itemService.update(item);
        log.info("Item with id={} was updated", updatedItem.getId());
        return ItemMapper.toItemDto(updatedItem);
    }
}
