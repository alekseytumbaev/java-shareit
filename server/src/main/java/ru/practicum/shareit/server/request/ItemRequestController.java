package ru.practicum.shareit.server.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.request.model.dto.ItemRequestRequestDto;
import ru.practicum.shareit.server.request.model.dto.ItemRequestResponseDto;
import ru.practicum.shareit.server.util.constant.Header;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestResponseDto add(@RequestHeader(Header.USER_ID_HEADER) long authorId,
                                      @RequestBody ItemRequestRequestDto itemRequestDto) {
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
                                                                 @RequestParam int from, @RequestParam int size) {
        Collection<ItemRequestResponseDto> responseDtos = itemRequestService
                .getAllExceptAuthorIdSortedByCreatedDesc(from, size, authorId);
        log.info("Get {} item requests from index={} by user with id={}", size, from, authorId);
        return responseDtos;
    }
}
