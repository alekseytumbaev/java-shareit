package ru.practicum.shareit.server.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.server.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.server.item.ItemService;
import ru.practicum.shareit.server.item.model.dto.ItemDto;
import ru.practicum.shareit.server.user.UserService;
import ru.practicum.shareit.server.user.model.UserDto;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class BookingServiceWithDatabaseTest {

    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Test
    @DisplayName("Should get booking dto by id")
    void getById() {
        UserDto owner = userService.add(new UserDto(0L, "owner", "owner@mail.ru"));
        ItemDto item = itemService.add(new ItemDto(0L, "item", "item", true, owner.getId(), 0));

        UserDto booker = userService.add(new UserDto(0L, "booker", "booker@mail.ru"));
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto = bookingService.add(
                new BookingRequestDto(now.plusDays(2), now.plusDays(3), item.getId()), booker.getId()
        );

        BookingResponseDto bookingByBooker = bookingService.getDtoById(bookingResponseDto.getId(), booker.getId());
        assertEquals(bookingResponseDto, bookingByBooker);

        BookingResponseDto bookingByOwner = bookingService.getDtoById(bookingResponseDto.getId(), owner.getId());
        assertEquals(bookingResponseDto, bookingByOwner);
    }

    @Test
    @DisplayName("Should add new booking")
    void add() {
        UserDto owner = userService.add(new UserDto(0L, "owner", "owner@mail.ru"));
        ItemDto item = itemService.add(new ItemDto(0L, "item", "item", true, owner.getId(), 0));
        UserDto booker = userService.add(new UserDto(0L, "booker", "booker@mail.ru"));
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto = bookingService.add(
                new BookingRequestDto(now.plusDays(2), now.plusDays(3), item.getId()), booker.getId()
        );

        Assertions.assertEquals(bookingResponseDto, bookingService.getDtoById(bookingResponseDto.getId(), booker.getId()));
        assertEquals(bookingResponseDto.getStatus(), BookingStatus.WAITING);
    }

    @Test
    @DisplayName("Should change status of booking")
    void changeStatus() {
        UserDto owner = userService.add(new UserDto(0L, "owner", "owner@mail.ru"));
        ItemDto item = itemService.add(new ItemDto(0L, "item", "item", true, owner.getId(), 0));
        UserDto booker = userService.add(new UserDto(0L, "booker", "booker@mail.ru"));
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto = bookingService.add(
                new BookingRequestDto(now.plusDays(2), now.plusDays(3), item.getId()), booker.getId()
        );

        bookingService.changeStatus(bookingResponseDto.getId(), owner.getId(), true);
        assertEquals(
                BookingStatus.APPROVED,
                bookingService.getDtoById(bookingResponseDto.getId(), owner.getId()).getStatus()
        );
    }

    @Test
    @DisplayName("Should get all by booker id sorted by start time desc")
    void getAllByBookerIdSortedByStartTimeDesc() {
        UserDto owner = userService.add(new UserDto(0L, "owner", "owner@mail.ru"));
        ItemDto item = itemService.add(new ItemDto(0L, "item", "item", true, owner.getId(), 0));
        UserDto booker = userService.add(new UserDto(0L, "booker", "booker@mail.ru"));
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto1 = bookingService.add(
                new BookingRequestDto(now.plusDays(2), now.plusDays(3), item.getId()), booker.getId()
        );
        BookingResponseDto bookingResponseDto2 = bookingService.add(
                new BookingRequestDto(now.plusDays(4), now.plusDays(5), item.getId()), booker.getId()
        );

        Collection<BookingResponseDto> bookingResponseDtos = bookingService
                .getAllByBookerIdSortedByStartTimeDesc(booker.getId(), BookingState.ALL, 0, 10);
        assertEquals(2, bookingResponseDtos.size());
        Iterator<BookingResponseDto> iterator = bookingResponseDtos.iterator();
        assertEquals(bookingResponseDto2, iterator.next());
        assertEquals(bookingResponseDto1, iterator.next());
    }

    @Test
    @DisplayName("Should get all by owner id sorted by start time desc")
    void getAllByOwnerIdSortedByStartTimeDesc() {
        UserDto owner = userService.add(new UserDto(0L, "owner", "owner@mail.ru"));
        ItemDto item1 = itemService.add(new ItemDto(0L, "item1", "item1", true, owner.getId(), 0));
        ItemDto item2 = itemService.add(new ItemDto(0L, "item2", "item2", true, owner.getId(), 0));

        UserDto booker = userService.add(new UserDto(0L, "booker", "booker@mail.ru"));
        LocalDateTime now = LocalDateTime.now();
        BookingResponseDto bookingResponseDto1 = bookingService.add(
                new BookingRequestDto(now.plusDays(2), now.plusDays(3), item1.getId()), booker.getId()
        );
        BookingResponseDto bookingResponseDto2 = bookingService.add(
                new BookingRequestDto(now.plusDays(4), now.plusDays(5), item2.getId()), booker.getId()
        );

        Collection<BookingResponseDto> bookingResponseDtos = bookingService
                .getAllByItemOwnerIdSortedByStartTimeDesc(owner.getId(), BookingState.ALL, 0, 10);
        assertEquals(2, bookingResponseDtos.size());
        Iterator<BookingResponseDto> iterator = bookingResponseDtos.iterator();
        assertEquals(bookingResponseDto2, iterator.next());
        assertEquals(bookingResponseDto1, iterator.next());
    }
}
