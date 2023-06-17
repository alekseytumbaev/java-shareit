package ru.practicum.shareit.gateway.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.dto.CommentRequestDto;
import ru.practicum.shareit.gateway.item.dto.ItemDto;
import ru.practicum.shareit.gateway.util.constant.Header;
import ru.practicum.shareit.gateway.util.constraint_group.Creation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

@Slf4j
@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemClient itemClient;

    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody @Validated({Creation.class, Default.class}) ItemDto itemDto,
                                      @RequestHeader(Header.USER_ID_HEADER) long ownerId) {
        log.info("User with id={} is adding item with name={}", ownerId, itemDto.getName());
        ResponseEntity<Object> response = itemClient.add(ownerId, itemDto);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(Header.USER_ID_HEADER) long userId,
                                          @PathVariable long itemId) {
        log.info("User with id={} is retrieving item with id={}", userId, itemId);
        ResponseEntity<Object> response = itemClient.getById(itemId, userId);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchByNameOrDescription(@RequestHeader(Header.USER_ID_HEADER) long userId,
                                                            @RequestParam @NotNull @Size(max = 200) String text,
                                                            @RequestParam(defaultValue = "0") int from,
                                                            @RequestParam(defaultValue = "10") int size) {
        log.info("User with id={} is searching {} items by name or description starting from index {}, text for search = {}",
                userId, size, from, text);
        ResponseEntity<Object> response = itemClient.searchByNameOrDescription(userId, text, from, size);
        log.info("Response: status = {}, headers = {}", response.getStatusCode(), response.getHeaders());
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(Header.USER_ID_HEADER) long ownerId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        log.info("Owner with id={} is retrieving {} items starting from index {}", ownerId, size, from);
        ResponseEntity<Object> response = itemClient.getAllByOwnerId(ownerId, from, size);
        log.info("Response: status = {}, headers = {}", response.getStatusCode(), response.getHeaders());
        return response;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestBody @Valid ItemDto itemDto,
                                         @PathVariable long itemId,
                                         @RequestHeader(Header.USER_ID_HEADER) long userId) {
        log.info("User with id={} is updating item with id={}", userId, itemId);
        ResponseEntity<Object> response = itemClient.update(userId, itemId, itemDto);
        log.info("Response: {}", response);
        return response;
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestBody @Valid CommentRequestDto commentRequestDto,
                                             @PathVariable long itemId,
                                             @RequestHeader(Header.USER_ID_HEADER) long authorId) {
        log.info("User with id={} is adding comment to item with id={}", authorId, itemId);
        ResponseEntity<Object> response = itemClient.addComment(authorId, itemId, commentRequestDto);
        log.info("Response: {}", response);
        return response;
    }
}
