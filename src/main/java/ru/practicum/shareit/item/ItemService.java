package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.dto.SimpleBookingDtoResponse;
import ru.practicum.shareit.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsDtoResponse;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ItemService {
    private final ItemRepository itemRepo;
    private final UserService userService;
    private final BookingRepository bookingRepo;
    private final ItemMapper itemMapper;

    public ItemService(ItemRepository itemRepo, UserService userService, BookingRepository bookingRepo, ItemMapper itemMapper) {
        this.itemRepo = itemRepo;
        this.userService = userService;
        this.bookingRepo = bookingRepo;
        this.itemMapper = itemMapper;
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
    public ItemWithBookingsDtoResponse getDtoById(long itemId, long userId)
            throws ItemNotFoundException, UserNotFoundException {
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException(
                    String.format("Cannot get item, because user with id=%d not found", userId));
        }
        Item item = getById(itemId);
        ItemWithBookingsDtoResponse itemDto = itemMapper.toItemWithBookingsDtoResponse(item, null, null);

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

    public Collection<ItemWithBookingsDtoResponse> getAllByOwnerId(long ownerId) {
        Collection<Item> items = itemRepo.findAllByOwner_Id(ownerId);

        List<ItemWithBookingsDtoResponse> itemDtos = new ArrayList<>();
        for (Item item : items) {
            ItemWithBookingsDtoResponse itemDto = itemMapper.toItemWithBookingsDtoResponse(item, null, null);
            setItemLastAndFirstBookingsOrNulls(itemDto);
            itemDtos.add(itemDto);
        }

        //needed to pass the tests
        Comparator<ItemWithBookingsDtoResponse> comparator = (i1, i2) -> {
            SimpleBookingDtoResponse b1 = i1.getLastBooking();
            SimpleBookingDtoResponse b2 = i2.getLastBooking();
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

    private void setItemLastAndFirstBookingsOrNulls(ItemWithBookingsDtoResponse itemDto) {
        Collection<Booking> bookings = bookingRepo.findAllByItem_Id(itemDto.getId());

        Optional<Booking> lastBooking = getItemLastBooking(bookings);
        SimpleBookingDtoResponse lastBookingDto = lastBooking
                .map(BookingMapper::toSimpleBookingDtoResponse).orElse(null);

        Optional<Booking> nextBooking = getItemNextBooking(bookings);
        SimpleBookingDtoResponse nextBookingDto = nextBooking
                .map(BookingMapper::toSimpleBookingDtoResponse).orElse(null);

        itemDto.setLastBooking(lastBookingDto);
        itemDto.setNextBooking(nextBookingDto);
    }

    private Optional<Booking> getItemLastBooking(Collection<Booking> itemBookings) {
        //the later date is considered greater
        Comparator<LocalDateTime> timeComparator = (t1, t2) -> t1.isAfter(t2) ? 1 : -1;

        return itemBookings.stream()
                .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                .max((b1, b2) -> timeComparator.compare(b1.getEnd(), b2.getEnd()));
    }

    private Optional<Booking> getItemNextBooking(Collection<Booking> itemBookings) {
        //the later date is considered greater
        Comparator<LocalDateTime> timeComparator = (t1, t2) -> t1.isAfter(t2) ? 1 : -1;

        return itemBookings.stream()
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
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
