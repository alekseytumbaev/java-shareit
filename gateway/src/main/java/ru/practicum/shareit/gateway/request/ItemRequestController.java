package ru.practicum.shareit.gateway.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.gateway.util.constant.Header;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    public ItemRequestController(ItemRequestClient itemRequestClient) {
        this.itemRequestClient = itemRequestClient;
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(Header.USER_ID_HEADER) long authorId,
                                      @RequestBody @Valid ItemRequestRequestDto itemRequestDto) {
        log.info("User with id={} is adding item request with a description='{}'", authorId, itemRequestDto.getDescription());
        ResponseEntity<Object> response = itemRequestClient.add(authorId, itemRequestDto);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping
    public ResponseEntity<Object> getAllByAuthorIdSortedByCreatedDesc(@RequestHeader(Header.USER_ID_HEADER) long authorId) {
        log.info("User with id={} is retrieving all his item requests", authorId);
        ResponseEntity<Object> response = itemRequestClient.getAllByAuthorIdSortedByCreatedDesc(authorId);
        log.info("Response: status = {}, headers = {}", response.getStatusCode(), response.getHeaders());
        return response;
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(Header.USER_ID_HEADER) long userId,
                                          @PathVariable long requestId) {
        log.info("User with id={} is retrieving item request with id={}", requestId, userId);
        ResponseEntity<Object> response = itemRequestClient.getById(userId, requestId);
        log.info("Response: {}", response);
        return response;
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllExceptAuthor(@RequestHeader(Header.USER_ID_HEADER) long authorId,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                     @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("User with id={} is retrieving {} item requests except his starting from index {}", authorId, size, from);
        ResponseEntity<Object> response = itemRequestClient.getAllExceptAuthorIdSortedByCreatedDesc(authorId, from, size);
        log.info("Response: status = {}, headers = {}", response.getStatusCode(), response.getHeaders());
        return response;
    }
}
