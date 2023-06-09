package ru.practicum.shareit.server.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.item.model.dto.CommentRequestDto;
import ru.practicum.shareit.server.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.server.item.model.dto.ItemDto;
import ru.practicum.shareit.server.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.server.util.constant.Header;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto add(@RequestBody ItemDto itemDto,
                       @RequestHeader(Header.USER_ID_HEADER) long ownerId) {
        itemDto.setOwnerId(ownerId);
        ItemDto addedItem = itemService.add(itemDto);
        log.info("Item with id={} was added", addedItem.getId());
        return addedItem;
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsResponseDto getById(@RequestHeader(Header.USER_ID_HEADER) long userId,
                                               @PathVariable long itemId) {
        ItemWithBookingsResponseDto itemDto = itemService.getDtoById(itemId, userId);
        log.info("Item with id={} retrieved", itemDto.getId());
        return itemDto;
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchByNameOrDescription(@RequestParam String text,
                                                         @RequestParam(defaultValue = "0") int from,
                                                         @RequestParam(defaultValue = "10") int size) {
        Collection<ItemDto> items = itemService.searchByNameOrDescription(text, from, size);
        log.info("Items with '{}' in name or description retrieved", text);
        return items;
    }

    @GetMapping
    public Collection<ItemWithBookingsResponseDto> getAllByOwnerId(@RequestHeader(Header.USER_ID_HEADER) long ownerId,
                                                                   @RequestParam(defaultValue = "0") int from,
                                                                   @RequestParam(defaultValue = "10") int size) {
        Collection<ItemWithBookingsResponseDto> itemsDto = itemService.getAllByOwnerId(ownerId, from, size);
        log.info("Items of owner with id={} retrieved", ownerId);
        return itemsDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @PathVariable long itemId,
                          @RequestHeader(Header.USER_ID_HEADER) long userId) {
        itemDto.setOwnerId(userId);
        itemDto.setId(itemId);
        ItemDto updatedItem = itemService.update(itemDto);
        log.info("Item with id={} was updated", updatedItem.getId());
        return updatedItem;
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestBody CommentRequestDto commentRequestDto,
                                         @PathVariable long itemId,
                                         @RequestHeader(Header.USER_ID_HEADER) long authorId) {
        CommentResponseDto comment = itemService.addComment(commentRequestDto, itemId, authorId);
        log.info("Comment with id={} for item with id={} was created", comment.getId(), itemId);
        return comment;
    }
}
