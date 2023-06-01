package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.exception.UnknownBookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.util.constant.Header;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    public BookingController(BookingService bookingService, BookingMapper bookingMapper) {
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
    }

    @PostMapping
    public BookingResponseDto add(@RequestBody @NotNull @Valid BookingRequestDto bookingRequestDto,
                                  @RequestHeader(Header.USER_ID_HEADER) long bookerId) {
        Booking addedBooking = bookingService.add(
                bookingMapper.toBooking(bookingRequestDto, 0, bookerId, BookingStatus.WAITING));
        log.info("Booking with id={} was added", addedBooking.getId());
        return bookingMapper.toBookingResponseDto(addedBooking);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto changeStatus(@PathVariable long bookingId,
                                           @RequestParam boolean approved,
                                           @RequestHeader(Header.USER_ID_HEADER) long userId) {
        BookingResponseDto bookingResponseDto = bookingMapper.toBookingResponseDto(
                bookingService.changeStatus(bookingId, userId, approved));
        log.info("Status of booking with id={} was changed", bookingId);
        return bookingResponseDto;
    }

    @GetMapping
    public Collection<BookingResponseDto> getAllSortedByStartTimeDesc(@RequestHeader(Header.USER_ID_HEADER) long bookerId,
                                                                      @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = convertToBookingState(state);

        Collection<Booking> bookings = bookingService.getAllSortedByStartTimeDesc(bookerId, bookingState);
        List<BookingResponseDto> bookingResponseDtos = bookings.stream()
                .map(bookingMapper::toBookingResponseDto).collect(Collectors.toList());
        log.info("Bookings with state={} were retrieved by booker with id={}", state, bookerId);
        return bookingResponseDtos;
    }

    @GetMapping("/owner")
    public Collection<BookingResponseDto> getAllForUserItemsSortedByStartTimeDesc(
            @RequestHeader(Header.USER_ID_HEADER) long itemOwnerId,
            @RequestParam(defaultValue = "ALL") String state) {
        BookingState bookingState = convertToBookingState(state);

        Collection<Booking> bookings = bookingService.getAllForUserItemsSortedByStartTimeDesc(itemOwnerId, bookingState);
        log.info("Bookings with state={} were retrieved by items owner with id={}", state, itemOwnerId);
        return bookings.stream().map(bookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@PathVariable long bookingId,
                                      @RequestHeader(Header.USER_ID_HEADER) long userId) {
        BookingResponseDto bookingResponseDto = bookingMapper.toBookingResponseDto(
                bookingService.getById(bookingId, userId));
        log.info("Booking with id={} was retrieved", bookingId);
        return bookingResponseDto;
    }

    private BookingState convertToBookingState(String state) throws UnknownBookingState {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknownBookingState("Unknown state: " + state, state);
        }
    }
}
