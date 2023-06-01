package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.exception.BookingAlreadyApprovedException;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.ItemUnavailableException;
import ru.practicum.shareit.booking.exception.SameItemOwnerAndBookerIdException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.global_exception.UnauthorizedException;
import ru.practicum.shareit.error.global_exception.UserNotFoundException;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.Optional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;

    public BookingService(BookingRepository bookingRepository, UserService userService) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
    }

    public Booking getById(long id, long userId) throws BookingNotFoundException, UnauthorizedException {
        Booking booking = getById(id);
        if (booking.getItem().getOwner().getId() != userId &&
                booking.getBooker().getId() != userId) {
            throw new UnauthorizedException(
                    String.format("User with id=%d cannot retrieve booking with id=%d," +
                            "because he's neither booker nor owner", userId, id));
        }
        return booking;
    }

    public Booking getById(long id) throws BookingNotFoundException {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException(String.format("Booking with id=%d not found", id));
        }
        return bookingOpt.get();
    }

    public Booking add(Booking booking) throws ItemUnavailableException {
        if (booking.getItem().getOwner().getId() == booking.getBooker().getId()) {
            throw new SameItemOwnerAndBookerIdException(
                    String.format("Cannot add booking, because booker and owner are the same user with id=%d",
                            booking.getBooker().getId()));
        }
        if (!booking.getItem().getAvailable()) {
            throw new ItemUnavailableException(
                    String.format("Cannot book item with id=%d, because it is not available", booking.getItem().getId()));
        }
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.save(booking);
    }

    public Booking changeStatus(long bookingId, long itemOwnerId, boolean approved)
            throws UnauthorizedException, BookingNotFoundException {
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
        return bookingRepository.save(booking);
    }

    public Collection<Booking> getAllSortedByStartTimeDesc(long bookerId, BookingState state)
    throws BookingNotFoundException {
        if (!userService.existsById(bookerId)) {
            throw new UserNotFoundException(
                    String.format("Cannot retrieve user's bookings, because user with id=%d not found", bookerId));
        }
        switch (state) {
            case CURRENT:
                return bookingRepository.findAllCurrentByBooker_IdOrderByStartDesc(bookerId);
            case PAST:
                return bookingRepository.findAllPastByBooker_IdOrderByStartDesc(bookerId);
            case FUTURE:
                return bookingRepository.findAllFutureByBooker_IdOrderByStartDesc(bookerId);
            case REJECTED:
                return bookingRepository.findAllByStatusAndBooker_IdOrderByStartDesc(BookingStatus.REJECTED, bookerId);
            case WAITING:
                return bookingRepository.findAllByStatusAndBooker_IdOrderByStartDesc(BookingStatus.WAITING, bookerId);
            default:
                return bookingRepository.findAllByBooker_IdOrderByStartDesc(bookerId);
        }
    }

    public Collection<Booking> getAllForUserItemsSortedByStartTimeDesc(long itemOwnerId, BookingState state)
            throws BookingNotFoundException {
        if (!userService.existsById(itemOwnerId)) {
            throw new UserNotFoundException(
                    String.format("User with id=%d not found, cannot retrieve bookings for user's items", itemOwnerId));
        }
        switch (state) {
            case CURRENT:
                return bookingRepository.findAllCurrentByItem_Owner_IdOrderByStartDesc(itemOwnerId);
            case PAST:
                return bookingRepository.findAllPastByItem_Owner_IdOrderByStartDesc(itemOwnerId);
            case FUTURE:
                return bookingRepository.findAllFutureByItem_Owner_IdOrderByStartDesc(itemOwnerId);
            case REJECTED:
                return bookingRepository.findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus.REJECTED, itemOwnerId);
            case WAITING:
                return bookingRepository.findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus.WAITING, itemOwnerId);
            default:
                return bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(itemOwnerId);
        }
    }
}
