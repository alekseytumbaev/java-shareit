package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponseDto;
import ru.practicum.shareit.util.constant.Header;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestResponseDto add(@RequestHeader(Header.USER_ID_HEADER) long authorId,
                                      @RequestBody @Valid ItemRequestRequestDto itemRequestDto) {
        ItemRequestResponseDto responseDto = itemRequestService.add(itemRequestDto, authorId);
        log.info("Item request with id={} was created by user with id={}", responseDto.getId(), authorId);
        return responseDto;
    }

    @GetMapping
    public Collection<ItemRequestResponseDto> getAllByAuthorIdSortedByCreatedDesc(
            @RequestHeader(Header.USER_ID_HEADER) long authorId) {
        Collection<ItemRequestResponseDto> responseDtos = itemRequestService.getAllByAuthorIdSortedByCreatedDesc(authorId);
        log.info("Get all item requests by user with id={}", authorId);
        return responseDtos;
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(@RequestHeader(Header.USER_ID_HEADER) long userId,
                                          @PathVariable long requestId) {
        ItemRequestResponseDto responseDto = itemRequestService.getById(userId, requestId);
        log.info("Get item request with id={} by user with id={}", requestId, userId);
        return responseDto;
    }

    @GetMapping("/all")
    public Collection<ItemRequestResponseDto> getAllExceptAuthor(@RequestHeader(Header.USER_ID_HEADER) long authorId,
                                                                 @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                                 @RequestParam(defaultValue = "10") @Positive int size) {
        Collection<ItemRequestResponseDto> responseDtos = itemRequestService
                .getAllExceptAuthorIdSortedByCreatedDesc(from, size, authorId);
        log.info("Get {} item requests from index={} by user with id={}", size, from, authorId);
        return responseDtos;
    }
}
