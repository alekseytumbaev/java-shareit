package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.model.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepo;
    private final UserService userService;
    private final ItemRepository itemRepo;

    public ItemRequestService(ItemRequestRepository itemRequestRepo, UserService userService, ItemRepository itemRepo) {
        this.itemRequestRepo = itemRequestRepo;
        this.userService = userService;
        this.itemRepo = itemRepo;
    }

    public ItemRequestResponseDto add(ItemRequestRequestDto itemRequestDto, long userId) throws UserNotFoundException {
        User author = userService.getById(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, 0, LocalDateTime.now(), author);
        return ItemRequestMapper.toItemRequestResponseDto(itemRequestRepo.save(itemRequest), null);
    }

    public Collection<ItemRequestResponseDto> getAllByAuthorIdSortedByCreatedDesc(long authorId)
            throws UserNotFoundException {
        User author = userService.getById(authorId);

        List<ItemRequest> itemRequests = itemRequestRepo.findAllByAuthor_IdOrderByCreatedDesc(author.getId());
        return convertToResponseDtoList(itemRequests);
    }

    public ItemRequestResponseDto getById(long userId, long requestId) {
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException(
                    String.format("Cannot get request with id=%d for user with id=%d, user not found", requestId, userId)
            );
        }
        ItemRequest itemRequest = itemRequestRepo.findById(requestId).orElseThrow(() ->
                new ItemRequestNotFoundException(String.format("Item request with id %d not found", requestId))
        );
        List<ItemRequestResponseDto> itemRequestDtos = convertToResponseDtoList(List.of(itemRequest));
        return itemRequestDtos.get(0);
    }

    public Collection<ItemRequestResponseDto> getAllExceptAuthorIdSortedByCreatedDesc(int from, int size, long authorId)
            throws UserNotFoundException {
        User author = userService.getById(authorId);
        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("created").descending());
        Page<ItemRequest> itemRequests = itemRequestRepo.findAllExceptAuthor_Id(pageRequest, author.getId());
        return convertToResponseDtoList(itemRequests);
    }

    private List<ItemRequestResponseDto> convertToResponseDtoList(Iterable<ItemRequest> itemRequests) {
        List<ItemRequestResponseDto> itemRequestResponseDtos = new LinkedList<>();
        List<Long> requestIds = new LinkedList<>();
        for (ItemRequest itemRequest : itemRequests) {
            requestIds.add(itemRequest.getId());
        }

        Map<Long, List<Item>> requestIdToResponses = itemRepo.findAllByRequest_IdAsMap(requestIds);
        for (ItemRequest itemRequest : itemRequests) {
            List<Item> responses = requestIdToResponses.get(itemRequest.getId());
            responses = responses == null ? new LinkedList<>() : responses;
            List<ItemDto> responseDtos = responses.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
            itemRequestResponseDtos.add(ItemRequestMapper.toItemRequestResponseDto(itemRequest, responseDtos));
        }
        return itemRequestResponseDtos;
    }

}
