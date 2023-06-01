package ru.practicum.shareit.booking.model;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.model.UserMapper;

@Component
public class BookingMapper {
    private final ItemService itemService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public BookingMapper(ItemService itemService, UserService userService, UserMapper userMapper, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.userService = userService;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
    }

    public BookingResponseDto toBookingResponseDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                userMapper.toUserDto(booking.getBooker()),
                itemMapper.toItemDto(booking.getItem()),
                booking.getStatus());
    }

    public Booking toBooking(BookingRequestDto bookingRequestDto, long bookingId, long bookerId, BookingStatus bookingStatus) {
        Item item = itemService.getById(bookingRequestDto.getItemId());
        User user = userService.getById(bookerId);

        return new Booking(
                bookingId,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                user,
                item,
                bookingStatus
        );
    }
}
