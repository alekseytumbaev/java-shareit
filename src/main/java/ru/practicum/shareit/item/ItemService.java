package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.dto.SimpleBookingResponseDto;
import ru.practicum.shareit.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.item.exception.CommentingRestrictedException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.model.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemRepository itemRepo;
    private final UserService userService;
    private final BookingRepository bookingRepo;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepo;

    public ItemService(ItemRepository itemRepo, UserService userService, BookingRepository bookingRepo,
                       ItemMapper itemMapper, CommentRepository commentRepo) {
        this.itemRepo = itemRepo;
        this.userService = userService;
        this.bookingRepo = bookingRepo;
        this.itemMapper = itemMapper;
        this.commentRepo = commentRepo;
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
                            "because he haven't booked this item", itemId, authorId));
        }

        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.APPROVED &&
                    booking.getEnd().isBefore(now)) {
                break;
            }
            throw new CommentingRestrictedException(
                    String.format("User with id=%d cannot write comment for item with id=%d, " +
                            "because his booking haven't ended yet, or it wasn't approved", authorId, itemId));
        }

        Comment comment = commentRepo.save(CommentMapper.toComment(commentRequestDto, now, item, author));
        return CommentMapper.toCommentResponseDto(comment);
    }

    public Item add(Item item) throws UserNotFoundException {
        if (!userService.existsById(item.getOwner().getId())) {
            throw new UserNotFoundException(
                    String.format("Cannot add item, because owner with id=%d not found", item.getOwner().getId()));
        }
        return itemRepo.save(item);
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

        List<CommentResponseDto> commentResponseDtos = commentRepo.findByItem_Id(itemId)
                .stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList());
        ItemWithBookingsResponseDto itemDto = itemMapper.toItemWithBookingsResponseDto(item, null, null,
                commentResponseDtos);

        if (item.getOwner().getId() == userId) {
            setItemLastAndFirstBookingsOrNulls(itemDto);
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

    public Collection<ItemWithBookingsResponseDto> getAllByOwnerId(long ownerId) {
        Collection<Item> items = itemRepo.findAllByOwner_Id(ownerId);

        List<ItemWithBookingsResponseDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            List<CommentResponseDto> commentResponseDtos = commentRepo.findByItem_Id(item.getId())
                    .stream().map(CommentMapper::toCommentResponseDto).collect(Collectors.toList());
            ItemWithBookingsResponseDto itemDto = itemMapper.toItemWithBookingsResponseDto(item, null, null,
                    commentResponseDtos);
            setItemLastAndFirstBookingsOrNulls(itemDto);
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

    private void setItemLastAndFirstBookingsOrNulls(ItemWithBookingsResponseDto itemDto) {
        Collection<Booking> bookings = bookingRepo.findAllByItem_Id(itemDto.getId());

        Optional<Booking> lastBooking = getItemLastBooking(bookings);
        SimpleBookingResponseDto lastBookingDto = lastBooking
                .map(BookingMapper::toSimpleBookingResponseDto).orElse(null);

        Optional<Booking> nextBooking = getItemNextBooking(bookings);
        SimpleBookingResponseDto nextBookingDto = nextBooking
                .map(BookingMapper::toSimpleBookingResponseDto).orElse(null);

        itemDto.setLastBooking(lastBookingDto);
        itemDto.setNextBooking(nextBookingDto);
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

    public Collection<Item> searchByNameOrDescription(String text) {
        if (text.isBlank()) {
            return new ArrayList<>(0);
        }
        return itemRepo.searchByNameOrDescription(text);
    }

    public Item update(Item item) throws ItemNotFoundException {
        Item presentedItem = getById(item.getId());

        if (item.getOwner().getId() != presentedItem.getOwner().getId()) {
            throw new UnauthorizedException(String.format("User with id=%d cannot update item owned by user with id=%d",
                    item.getOwner().getId(), presentedItem.getOwner().getId()));
        }

        //replace null fields with values from existing item
        if (item.getName() == null) {
            item.setName(presentedItem.getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(presentedItem.getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(presentedItem.getAvailable());
        }
        item.setRequest(presentedItem.getRequest());

        return itemRepo.save(item);
    }
}
