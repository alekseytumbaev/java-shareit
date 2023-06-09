package ru.practicum.shareit.server.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.booking.exception.BookingAlreadyApprovedException;
import ru.practicum.shareit.server.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.server.booking.exception.ItemUnavailableException;
import ru.practicum.shareit.server.booking.exception.SameItemOwnerAndBookerIdException;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingMapper;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.server.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.server.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.server.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.item.exception.ItemNotFoundException;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.model.ItemMapper;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.model.UserMapper;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final UserService userService;
    private final ItemService itemService;

    public BookingService(BookingRepository bookingRepo, UserService userService, ItemService itemService) {
        this.bookingRepo = bookingRepo;
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
        Optional<Booking> bookingOpt = bookingRepo.findById(id);
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id=%d not found", id));
        }
        return bookingOpt.get();
    }

    public BookingResponseDto add(BookingRequestDto bookingRequestDto, long bookerId)
            throws ItemUnavailableException, ItemNotFoundException, UserNotFoundException, SameItemOwnerAndBookerIdException {
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
                bookingRepo.save(booking),
                UserMapper.toUserDto(booker),
                ItemMapper.toItemDto(item)
        );
    }

    public BookingResponseDto changeStatus(long bookingId, long itemOwnerId, boolean approved)
            throws UnauthorizedException, BookingNotFoundException, UserNotFoundException, ItemNotFoundException,
            BookingAlreadyApprovedException {

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
                bookingRepo.save(booking),
                UserMapper.toUserDto(booking.getBooker()),
                ItemMapper.toItemDto(booking.getItem())
        );
    }

    public Collection<BookingResponseDto> getAllByBookerIdSortedByStartTimeDesc(long bookerId, BookingState state,
                                                                                int from, int size)
            throws UserNotFoundException {
        if (!userService.existsById(bookerId)) {
            throw new UserNotFoundException(
                    String.format("Cannot retrieve user's bookings, because user with id=%d not found", bookerId));
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("start").descending());
        Page<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepo.findAllCurrentByBooker_Id(bookerId, pageRequest);
                break;
            case PAST:
                bookings = bookingRepo.findAllPastByBooker_Id(bookerId, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepo.findAllFutureByBooker_Id(bookerId, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepo.findAllByStatusAndBooker_Id(BookingStatus.REJECTED, bookerId, pageRequest);
                break;
            case WAITING:
                bookings = bookingRepo.findAllByStatusAndBooker_Id(BookingStatus.WAITING, bookerId, pageRequest);
                break;
            default:
                bookings = bookingRepo.findAllByBooker_Id(bookerId, pageRequest);
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
            throws UserNotFoundException {
        if (!userService.existsById(itemOwnerId)) {
            throw new UserNotFoundException(
                    String.format("User with id=%d not found, cannot retrieve bookings for user's items", itemOwnerId));
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("start").descending());
        Page<Booking> bookings;
        switch (state) {
            case CURRENT:
                bookings = bookingRepo.findAllCurrentByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
                break;
            case PAST:
                bookings = bookingRepo.findAllPastByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepo.findAllFutureByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepo.findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus.REJECTED,
                        itemOwnerId, pageRequest);
                break;
            case WAITING:
                bookings = bookingRepo.findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus.WAITING,
                        itemOwnerId, pageRequest);
                break;
            default:
                bookings = bookingRepo.findAllByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
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
