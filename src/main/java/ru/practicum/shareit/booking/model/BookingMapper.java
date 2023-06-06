package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.dto.SimpleBookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserDto;

public class BookingMapper {

    public static BookingResponseDto toBookingResponseDto(Booking booking, UserDto booker, ItemDto item) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booker,
                item,
                booking.getStatus());
    }

    public static Booking toBooking(BookingRequestDto bookingRequestDto, long bookingId, User booker,
                             Item item, BookingStatus bookingStatus) {

        return new Booking(
                bookingId,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                booker,
                item,
                bookingStatus
        );
    }

    public static SimpleBookingResponseDto toSimpleBookingResponseDto(Booking booking) {
        return new SimpleBookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getBooker().getId(),
                booking.getItem().getId(),
                booking.getStatus()
        );
    }
}
