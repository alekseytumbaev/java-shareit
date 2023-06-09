package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.exception.BookingAlreadyApprovedException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.ItemUnavailableException;
import ru.practicum.shareit.booking.exception.SameItemOwnerAndBookerIdException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    public BookingService(BookingRepository bookingRepository, UserService userService, ItemService itemService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
    }

    public BookingResponseDto getDtoById(long id, long userId) throws BookingNotFoundException, UnauthorizedException {
        Booking booking = getById(id);
        if (booking.getItem().getOwner().getId() != userId &&
                booking.getBooker().getId() != userId) {
            throw new UnauthorizedException(
                    String.format("User with id=%d cannot retrieve booking with id=%d," +
                            "because he's neither booker nor owner", userId, id));
        }
        return BookingMapper.toBookingResponseDto(
                booking,
                UserMapper.toUserDto(booking.getBooker()),
                ItemMapper.toItemDto(booking.getItem())
        );
    }

    public Booking getById(long id) throws BookingNotFoundException {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id=%d not found", id));
        }
        return bookingOpt.get();
    }

    public BookingResponseDto add(BookingRequestDto bookingRequestDto, long bookerId)
            throws ItemUnavailableException, ItemNotFoundException, UserNotFoundException {
        Item item = itemService.getById(bookingRequestDto.getItemId());
        User booker = userService.getById(bookerId);

        if (item.getOwner().getId() == booker.getId()) {
            throw new SameItemOwnerAndBookerIdException(
                    String.format("Cannot add booking, because booker and owner are the same user with id=%d",
                            booker.getId()));
        }
        if (!item.getAvailable()) {
            throw new ItemUnavailableException(
                    String.format("Cannot book item with id=%d, because it is not available", item.getId()));
        }
        Booking booking = BookingMapper.toBooking(bookingRequestDto, 0, booker, item, BookingStatus.WAITING);
        booking.setStatus(BookingStatus.WAITING);
        return BookingMapper.toBookingResponseDto(
                bookingRepository.save(booking),
                UserMapper.toUserDto(booker),
                ItemMapper.toItemDto(item)
        );
    }

    public BookingResponseDto changeStatus(long bookingId, long itemOwnerId, boolean approved)
            throws UnauthorizedException, BookingNotFoundException, UserNotFoundException, ItemNotFoundException {

        Booking booking = getById(bookingId);
        if (booking.getItem().getOwner().getId() != itemOwnerId) {
            throw new UnauthorizedException(
                    String.format("User with id=%d cannot change status of booking with id=%d, " +
                                    "because item with id=%d does not belong to him",
                            itemOwnerId, bookingId, booking.getItem().getId()));
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BookingAlreadyApprovedException(String.format("Booking with id=%d is already approved", bookingId));
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingResponseDto(
                bookingRepository.save(booking),
                UserMapper.toUserDto(booking.getBooker()),
                ItemMapper.toItemDto(booking.getItem())
        );
    }

    public Collection<BookingResponseDto> getAllByBookerIdSortedByStartTimeDesc(long bookerId, BookingState state,
                                                                                int from, int size)
            throws BookingNotFoundException {
        if (!userService.existsById(bookerId)) {
            throw new UserNotFoundException(
                    String.format("Cannot retrieve user's bookings, because user with id=%d not found", bookerId));
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("start").descending());
        Page<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findAllCurrentByBooker_Id(bookerId, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findAllPastByBooker_Id(bookerId, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureByBooker_Id(bookerId, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByStatusAndBooker_Id(BookingStatus.REJECTED, bookerId, pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByStatusAndBooker_Id(BookingStatus.WAITING, bookerId, pageRequest);
                break;
            default:
                bookings = bookingRepository.findAllByBooker_Id(bookerId, pageRequest);
        }

        return bookings.stream()
                .map(b ->
                        BookingMapper.toBookingResponseDto(
                                b,
                                UserMapper.toUserDto(b.getBooker()),
                                ItemMapper.toItemDto(b.getItem())
                        )
                )
                .collect(Collectors.toList());
    }

    public Collection<BookingResponseDto> getAllByItemOwnerIdSortedByStartTimeDesc(long itemOwnerId, BookingState state,
                                                                                   int from, int size)
            throws BookingNotFoundException {
        if (!userService.existsById(itemOwnerId)) {
            throw new UserNotFoundException(
                    String.format("User with id=%d not found, cannot retrieve bookings for user's items", itemOwnerId));
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("start").descending());
        Page<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findAllCurrentByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findAllPastByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus.REJECTED,
                        itemOwnerId, pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus.WAITING,
                        itemOwnerId, pageRequest);
                break;
            default:
                bookings = bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
        }

        return bookings.stream().map(b ->
                        BookingMapper.toBookingResponseDto(
                                b,
                                UserMapper.toUserDto(b.getBooker()),
                                ItemMapper.toItemDto(b.getItem()))
                )
                .collect(Collectors.toList());
    }
}
