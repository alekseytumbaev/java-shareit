package ru.practicum.shareit.gateway.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookingRequestDto;
import ru.practicum.shareit.gateway.booking.exception.UnknownBookingStateException;
import ru.practicum.shareit.gateway.util.constant.Header;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody @NotNull @Valid BookingRequestDto bookingRequestDto,
                                      @RequestHeader(Header.USER_ID_HEADER) long bookerId) {
        log.info("User with id={} is adding booking for item with id={}", bookerId, bookingRequestDto.getItemId());
        return bookingClient.add(bookerId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> changeStatus(@PathVariable long bookingId,
                                               @RequestParam boolean approved,
                                               @RequestHeader(Header.USER_ID_HEADER) long userId) {
        log.info("User with id={} is changing booking status to '{}' for booking with id={}", userId, approved, bookingId);
        return bookingClient.changeStatus(bookingId, approved, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllSortedByStartTimeDesc(
            @RequestHeader(Header.USER_ID_HEADER) long bookerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        BookingState bookingState = convertToBookingState(state);

        log.info("Booker with id={} is retrieving {} bookings with state '{}' starting from index {}",
                bookerId, size, state, from);
        return bookingClient.getAllByBookerIdSortedByStartTimeDesc(bookerId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByItemOwnerIdSortedByStartTimeDesc(
            @RequestHeader(Header.USER_ID_HEADER) long itemOwnerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        BookingState bookingState = convertToBookingState(state);

        log.info("Item's owner id={} is retrieving {} bookings with state '{}' starting from index {}",
                itemOwnerId, size, state, from);
       return bookingClient.getAllByItemOwnerIdSortedByStartTimeDesc(itemOwnerId, bookingState, from, size);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@PathVariable long bookingId,
                                      @RequestHeader(Header.USER_ID_HEADER) long userId) {
        log.info("User with id={} is retrieving booking with id={}", userId, bookingId);
        return bookingClient.getById(bookingId, userId);
    }

    private BookingState convertToBookingState(String state) throws UnknownBookingStateException {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknownBookingStateException("Unknown state: " + state, state);
        }
    }
}
