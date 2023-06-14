package ru.practicum.shareit.server.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.booking.BookingRepository;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingMapper;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.model.dto.SimpleBookingResponseDto;
import ru.practicum.shareit.server.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.server.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.server.item.exception.CommentingRestrictedException;
import ru.practicum.shareit.server.item.exception.ItemNotFoundException;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.CommentMapper;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.model.ItemMapper;
import ru.practicum.shareit.server.item.model.dto.CommentRequestDto;
import ru.practicum.shareit.server.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.server.item.model.dto.ItemDto;
import ru.practicum.shareit.server.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemRepository itemRepo;
    private final UserService userService;
    private final BookingRepository bookingRepo;
    private final CommentRepository commentRepo;
    private final ItemRequestRepository requestRepo;

    public ItemService(ItemRepository itemRepo, UserService userService, BookingRepository bookingRepo,
                       CommentRepository commentRepo, ItemRequestRepository requestRepo) {
        this.itemRepo = itemRepo;
        this.userService = userService;
        this.bookingRepo = bookingRepo;
        this.commentRepo = commentRepo;
        this.requestRepo = requestRepo;
    }

    /**
     * User can write a comment for the item only if he booked it and the booking had ended before the comment was written
     */
    public CommentResponseDto addComment(CommentRequestDto commentRequestDto, long itemId, long authorId)
            throws UserNotFoundException, ItemNotFoundException {
        User author = userService.getById(authorId);
        Item item = getById(itemId);

        Collection<Booking> bookings = bookingRepo.findAllByItem_IdAndBooker_Id(itemId, authorId);
        if (bookings.isEmpty()) {
            throw new CommentingRestrictedException(
                    String.format("User with id=%d cannot write comment for item with id=%d, " +
                            "because he hasn't booked this item", itemId, authorId));
        }

        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.APPROVED &&
                    booking.getEnd().isBefore(now)) {
                break;
            }
            throw new CommentingRestrictedException(
                    String.format("User with id=%d cannot write comment for item with id=%d, " +
                            "because his booking hasn't ended yet, or it wasn't approved", authorId, itemId));
        }

        Comment comment = commentRepo.save(CommentMapper.toComment(commentRequestDto, 0, now, item, author));
        return CommentMapper.toCommentResponseDto(comment);
    }

    public ItemDto add(ItemDto itemDto) throws UserNotFoundException {
        itemDto.setId(0);
        User owner = userService.getById(itemDto.getOwnerId());
        if (itemDto.getRequestId() == 0) {
            Item item = ItemMapper.toItem(itemDto, owner, null);
            return ItemMapper.toItemDto(itemRepo.save(item));
        }

        ItemRequest request = getRequestById(itemDto.getRequestId());
        Item item = ItemMapper.toItem(itemDto, owner, request);
        return ItemMapper.toItemDto(itemRepo.save(item));
    }

    /**
     * Owner will be able to see item's last and next bookings
     */
    public ItemWithBookingsResponseDto getDtoById(long itemId, long userId)
            throws ItemNotFoundException, UserNotFoundException {
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException(
                    String.format("Cannot get item, because user with id=%d not found", userId));
        }
        Item item = getById(itemId);

        List<Long> idAsList = List.of(itemId);
        Map<Long, List<Comment>> itemIdToComments = commentRepo.findAllByItem_IdAsMap(idAsList);
        Map<Long, List<Booking>> itemIdToBookings = bookingRepo.findAllByItem_IdAsMap(idAsList);

        List<Comment> comments = itemIdToComments.get(itemId);
        List<CommentResponseDto> commentResponseDtos = comments == null ? new ArrayList<>() :
                comments.stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList());

        ItemWithBookingsResponseDto itemDto = ItemMapper.toItemWithBookingsResponseDto(item, null, null,
                commentResponseDtos);

        List<Booking> bookings = itemIdToBookings.get(itemId);
        if (item.getOwner().getId() == userId && bookings != null) {
            setItemLastAndNextBookingsOrNulls(itemDto, bookings);
        }
        return itemDto;
    }

    public Item getById(long itemId) throws ItemNotFoundException {
        Optional<Item> itemOpt = itemRepo.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new ItemNotFoundException(String.format("Item with id=%d not found", itemId));
        }
        return itemOpt.get();
    }

    public Collection<ItemWithBookingsResponseDto> getAllByOwnerId(long ownerId, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        Page<Item> items = itemRepo.findAllByOwner_Id(ownerId, pageRequest);

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        Map<Long, List<Comment>> itemIdToComments = commentRepo.findAllByItem_IdAsMap(itemIds);
        Map<Long, List<Booking>> itemIdToBookings = bookingRepo.findAllByItem_IdAsMap(itemIds);

        List<ItemWithBookingsResponseDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            List<Comment> comments = itemIdToComments.get(item.getId());
            List<CommentResponseDto> commentResponseDtos = comments == null ? new ArrayList<>() :
                    comments.stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList());

            ItemWithBookingsResponseDto itemDto = ItemMapper.toItemWithBookingsResponseDto(item, null, null,
                    commentResponseDtos);

            List<Booking> bookings = itemIdToBookings.get(item.getId());
            if (bookings != null) {
                setItemLastAndNextBookingsOrNulls(itemDto, bookings);
            }
            itemDtos.add(itemDto);
        }

        //needed to pass the tests
        Comparator<ItemWithBookingsResponseDto> comparator = (i1, i2) -> {
            SimpleBookingResponseDto b1 = i1.getLastBooking();
            SimpleBookingResponseDto b2 = i2.getLastBooking();
            if (b1 != null && b2 != null) {
                return b1.getStart().compareTo(b2.getStart());
            }
            if (b1 == null) {
                return 1;
            }
            if (b2 == null) {
                return -1;
            }
            return 0;
        };
        itemDtos.sort(comparator);

        return itemDtos;
    }

    private void setItemLastAndNextBookingsOrNulls(ItemWithBookingsResponseDto itemDto, List<Booking> itemBookings) {
        Booking lastBooking = getItemLastBooking(itemBookings).orElse(null);
        itemDto.setLastBooking(
                lastBooking == null ? null : BookingMapper.toSimpleBookingResponseDto(lastBooking)
        );
        Booking nextBooking = getItemNextBooking(itemBookings).orElse(null);
        itemDto.setNextBooking(
                nextBooking == null ? null : BookingMapper.toSimpleBookingResponseDto(nextBooking)
        );
    }

    private Optional<Booking> getItemLastBooking(Collection<Booking> itemBookings) {
        //the later date is considered greater
        Comparator<LocalDateTime> timeComparator = (t1, t2) -> t1.isAfter(t2) ? 1 : -1;

        return itemBookings.stream()
                .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                .max((b1, b2) -> timeComparator.compare(b1.getEnd(), b2.getEnd()));
    }

    private Optional<Booking> getItemNextBooking(Collection<Booking> itemBookings) {
        //the later date is considered greater
        Comparator<LocalDateTime> timeComparator = (t1, t2) -> t1.isAfter(t2) ? 1 : -1;

        return itemBookings.stream()
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()) &&
                        b.getStatus() != BookingStatus.CANCELED &&
                        b.getStatus() != BookingStatus.REJECTED)
                .min((b1, b2) -> timeComparator.compare(b1.getStart(), b2.getStart()));
    }

    public Collection<ItemDto> searchByNameOrDescription(String text, int from, int size) {
        if (text.isBlank()) {
            return new ArrayList<>(0);
        }
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return itemRepo.searchByNameOrDescription(text, pageRequest)
                .stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    public ItemDto update(ItemDto itemDto) throws ItemNotFoundException, ItemRequestNotFoundException {
        Item presentedItem = getById(itemDto.getId());

        if (itemDto.getOwnerId() != presentedItem.getOwner().getId()) {
            throw new UnauthorizedException(String.format("User with id=%d cannot update item owned by user with id=%d",
                    itemDto.getOwnerId(), presentedItem.getOwner().getId()));
        }

        //replace null fields with values from existing item
        if (itemDto.getName() == null) {
            itemDto.setName(presentedItem.getName());
        }
        if (itemDto.getDescription() == null) {
            itemDto.setDescription(presentedItem.getDescription());
        }
        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(presentedItem.getAvailable());
        }
        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() == 0) {
            if (presentedItem.getRequest() != null) {
                itemDto.setRequestId(presentedItem.getRequest().getId());
            }
        } else {
            itemRequest = getRequestById(itemDto.getRequestId());
        }

        Item item = ItemMapper.toItem(itemDto, presentedItem.getOwner(), itemRequest);
        return ItemMapper.toItemDto(itemRepo.save(item));
    }

    private ItemRequest getRequestById(long id) throws ItemRequestNotFoundException {
        return requestRepo.findById(id).orElseThrow(() ->
                new ItemRequestNotFoundException(String.format("Item request with id=%d not found", id))
        );
    }
}
