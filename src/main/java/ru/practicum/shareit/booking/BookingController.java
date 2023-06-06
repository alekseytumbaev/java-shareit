package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.exception.UnknownBookingStateException;
import ru.practicum.shareit.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.util.constant.Header;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto add(@RequestBody @NotNull @Valid BookingRequestDto bookingRequestDto,
                                  @RequestHeader(Header.USER_ID_HEADER) long bookerId) {
        BookingResponseDto addedBooking = bookingService.add(bookingRequestDto, bookerId);
        log.info("Booking with id={} was added", addedBooking.getId());
        return addedBooking;
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto changeStatus(@PathVariable long bookingId,
                                           @RequestParam boolean approved,
                                           @RequestHeader(Header.USER_ID_HEADER) long userId) {
        BookingResponseDto bookingResponseDto = bookingService.changeStatus(bookingId, userId, approved);
        log.info("Status of booking with id={} was changed", bookingId);
        return bookingResponseDto;
    }

    @GetMapping
    public Collection<BookingResponseDto> getAllSortedByStartTimeDesc(@RequestHeader(Header.USER_ID_HEADER) long bookerId,
                                                                      @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = convertToBookingState(state);

        Collection<BookingResponseDto> bookings = bookingService.getAllByBookerIdSortedByStartTimeDesc(bookerId, bookingState);
        log.info("Bookings with state={} were retrieved by booker with id={}", state, bookerId);
        return bookings;
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getAllForUserItemsSortedByStartTimeDesc(
            @RequestHeader(Header.USER_ID_HEADER) long itemOwnerId,
            @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = convertToBookingState(state);

        Collection<BookingResponseDto> bookings = bookingService.getAllByItemOwnerIdSortedByStartTimeDesc(itemOwnerId, bookingState);
        log.info("Bookings with state={} were retrieved by items owner with id={}", state, itemOwnerId);
        return bookings;
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@PathVariable long bookingId,
                                      @RequestHeader(Header.USER_ID_HEADER) long userId) {
        BookingResponseDto bookingResponseDto = bookingService.getDtoById(bookingId, userId);
        log.info("Booking with id={} was retrieved", bookingId);
        return bookingResponseDto;
    }

    private BookingState convertToBookingState(String state) throws UnknownBookingStateException {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknownBookingStateException("Unknown state: " + state, state);
        }
    }
}
