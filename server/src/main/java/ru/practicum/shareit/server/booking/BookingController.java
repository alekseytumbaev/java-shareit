package ru.practicum.shareit.server.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.server.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.server.util.constant.Header;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto add(@RequestBody BookingRequestDto bookingRequestDto,
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
    public Collection<BookingResponseDto> getAllSortedByStartTimeDesc(
            @RequestHeader(Header.USER_ID_HEADER) long bookerId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        Collection<BookingResponseDto> bookings = bookingService
                .getAllByBookerIdSortedByStartTimeDesc(bookerId, state, from, size);
        log.info("Bookings with state={} were retrieved by booker with id={}", state, bookerId);
        return bookings;
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getAllByItemOwnerIdSortedByStartTimeDesc(
            @RequestHeader(Header.USER_ID_HEADER) long itemOwnerId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        Collection<BookingResponseDto> bookings = bookingService
                .getAllByItemOwnerIdSortedByStartTimeDesc(itemOwnerId, state, from, size);
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
}
