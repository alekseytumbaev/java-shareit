package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemService itemService;

    @InjectMocks
    private BookingService bookingService;


    @Test
    @DisplayName("Should throw UserNotFoundException when the given bookerId does not exist")
    void getAllByByItemOwnerIdSortedByStartTimeDescWhenUserNotFound() {
        long bookerId = 1L;
        when(userService.existsById(bookerId)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> bookingService.getAllByItemOwnerIdSortedByStartTimeDesc(bookerId, BookingState.CURRENT, 0, 10)
        );
    }

    @Test
    @DisplayName("Should return all bookings by bookerId sorted by start time in descending order")
    void getAllByByItemOwnerIdSortedByStartTimeDesc() throws UserNotFoundException {
        long itemOwnerId = 1L;
        BookingState state = BookingState.ALL;
        int from = 0;
        int size = 10;
        User booker = new User(1L, "name", "email");
        User owner = new User(1L, "name", "email");
        Item item = new Item(itemOwnerId, "name", "description", true, owner, null);
        List<Booking> bookings = List.of(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), booker, item, BookingStatus.WAITING),
                new Booking(2L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(2), booker,
                        item, BookingStatus.WAITING),
                new Booking(3L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusHours(3), booker,
                        item, BookingStatus.WAITING)
        );
        Page<Booking> page = new PageImpl<>(bookings);
        when(userService.existsById(itemOwnerId)).thenReturn(true);
        when(bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(anyLong(), any(PageRequest.class))).thenReturn(page);

        Collection<BookingResponseDto> result = bookingService
                .getAllByItemOwnerIdSortedByStartTimeDesc(itemOwnerId, state, from, size);

        assertEquals(3, result.size());
        assertEquals(1L, result.iterator().next().getId());
    }

    @Test
    @DisplayName("Should return empty list when no bookings are found for the given bookerId")
    void getAllByByItemOwnerIdSortedByStartTimeDescWhenNoBookingsFound() {
        long itemOwnerId = 1L;
        int from = 0;
        int size = 10;
        BookingState state = BookingState.ALL;

        when(userService.existsById(itemOwnerId)).thenReturn(true);
        when(bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(anyLong(), any())).thenReturn(Page.empty());

        Collection<BookingResponseDto> bookings = bookingService.getAllByItemOwnerIdSortedByStartTimeDesc(itemOwnerId, state, from, size);

        assertTrue(bookings.isEmpty());
        verify(userService, times(1)).existsById(itemOwnerId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("start").descending());
        verify(bookingRepository, times(1)).findAllByItem_Owner_IdOrderByStartDesc(itemOwnerId, pageRequest);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when the given bookerId does not exist")
    void getAllByBookerIdSortedByStartTimeDescWhenUserNotFound() {
        long bookerId = 1L;
        when(userService.existsById(bookerId)).thenReturn(false);

        assertThrows(UserNotFoundException.class,
                () -> bookingService.getAllByBookerIdSortedByStartTimeDesc(bookerId, BookingState.CURRENT, 0, 10)
        );
    }

    @Test
    @DisplayName("Should return all bookings by bookerId sorted by start time in descending order")
    void getAllByBookerIdSortedByStartTimeDesc() throws UserNotFoundException {
        long bookerId = 1L;
        BookingState state = BookingState.ALL;
        int from = 0;
        int size = 10;
        User booker = new User(bookerId, "name", "email");
        User owner = new User(1L, "name", "email");
        Item item = new Item(1L, "name", "description", true, owner, null);
        List<Booking> bookings = List.of(
                new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), booker, item, BookingStatus.WAITING),
                new Booking(2L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(2), booker,
                        item, BookingStatus.WAITING),
                new Booking(3L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusHours(3), booker,
                        item, BookingStatus.WAITING)
        );
        Page<Booking> page = new PageImpl<>(bookings);
        when(userService.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBooker_Id(anyLong(), any(PageRequest.class))).thenReturn(page);

        Collection<BookingResponseDto> result = bookingService.getAllByBookerIdSortedByStartTimeDesc(bookerId, state, from, size);

        assertEquals(3, result.size());
        assertEquals(1L, result.iterator().next().getId());
    }

    @Test
    @DisplayName("Should return empty list when no bookings are found for the given bookerId")
    void getAllByBookerIdSortedByStartTimeDescWhenNoBookingsFound() {
        long bookerId = 1L;
        int from = 0;
        int size = 10;
        BookingState state = BookingState.ALL;

        when(userService.existsById(bookerId)).thenReturn(true);
        when(bookingRepository.findAllByBooker_Id(anyLong(), any())).thenReturn(Page.empty());

        Collection<BookingResponseDto> bookings = bookingService.getAllByBookerIdSortedByStartTimeDesc(bookerId, state, from, size);

        assertTrue(bookings.isEmpty());
        verify(userService, times(1)).existsById(bookerId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("start").descending());
        verify(bookingRepository, times(1)).findAllByBooker_Id(bookerId, pageRequest);
    }

    @Test
    @DisplayName("Should throw a BookingNotFoundException when the booking with the given ID does not exist")
    void changeStatusWhenBookingNotFoundThenThrowBookingNotFoundException() {
        long bookingId = 1L;
        long itemOwnerId = 2L;
        boolean approved = true;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.changeStatus(bookingId, itemOwnerId, approved));

        verify(bookingRepository, times(1)).findById(bookingId);
        verifyNoMoreInteractions(bookingRepository, userService, itemService);
    }

    @Test
    @DisplayName("Should change the booking status to approved when the item owner approves the booking")
    void changeStatusWhenItemOwnerApprovesBooking() {
        long bookingId = 1L;
        long itemOwnerId = 2L;
        boolean approved = true;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        User owner = new User();
        owner.setId(itemOwnerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(3L);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        BookingResponseDto expected = BookingMapper.toBookingResponseDto(
                booking,
                UserMapper.toUserDto(booker),
                ItemMapper.toItemDto(item)
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.changeStatus(bookingId, itemOwnerId, approved);

        assertEquals(expected.getId(), result.getId());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    @DisplayName("Should throw a BookingAlreadyApprovedException when the booking is already approved")
    void changeStatusWhenBookingAlreadyApprovedThenThrowBookingAlreadyApprovedException() {
        long bookingId = 1L;
        long itemOwnerId = 2L;
        boolean approved = true;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        User owner = new User();
        owner.setId(itemOwnerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(3L);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(BookingAlreadyApprovedException.class, () -> bookingService.changeStatus(bookingId, itemOwnerId, approved));
    }

    @Test
    @DisplayName("Should change the booking status to rejected when the item owner rejects the booking")
    void changeStatusWhenItemOwnerRejectsBooking() {
        long bookingId = 1L;
        long itemOwnerId = 2L;
        boolean approved = false;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        User owner = new User();
        owner.setId(itemOwnerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);

        User booker = new User();
        booker.setId(3L);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        BookingResponseDto expected = BookingMapper.toBookingResponseDto(
                booking,
                UserMapper.toUserDto(booker),
                ItemMapper.toItemDto(item)
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        BookingResponseDto result = bookingService.changeStatus(bookingId, itemOwnerId, approved);

        assertEquals(expected.getId(), result.getId());
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    @DisplayName("Should throw an UnauthorizedException when the user trying to change the booking status is not the item owner")
    void changeStatusWhenUserIsNotItemOwnerThenThrowUnauthorizedException() {
        long bookingId = 1L;
        long itemOwnerId = 2L;
        long notItemOwnerId = 3L;
        boolean approved = true;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);

        User owner = new User();
        owner.setId(itemOwnerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);

        booking.setItem(item);
        booking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () -> bookingService.changeStatus(bookingId, notItemOwnerId, approved));
    }

    @Test
    @DisplayName("Should throw an exception when the item is not found")
    void addBookingWhenItemNotFoundThenThrowException() {
        long itemId = 1L;

        long bookerId = 1L;
        User booker = new User();
        booker.setId(bookerId);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );
        when(itemService.getById(itemId)).thenThrow(new ItemNotFoundException("Item not found"));

        assertThrows(ItemNotFoundException.class, () -> bookingService.add(bookingRequestDto, bookerId));

        verify(itemService, times(1)).getById(itemId);
    }

    @Test
    @DisplayName("Should add a booking when the item is available and the booker is not the owner")
    void addBookingWhenItemIsAvailableAndBookerIsNotOwner() {
        long bookerId = 1L;
        long itemId = 1L;
        long bookingId = 1L;

        User booker = new User();
        booker.setId(bookerId);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item(itemId, "name", "description", true, owner, null);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );

        Booking booking = BookingMapper.toBooking(
                bookingRequestDto,
                bookingId,
                booker,
                item,
                BookingStatus.WAITING
        );

        when(itemService.getById(itemId)).thenReturn(item);
        when(userService.getById(bookerId)).thenReturn(booker);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto bookingResponseDto = bookingService.add(bookingRequestDto, bookerId);
        assertEquals(bookingId, bookingResponseDto.getId());
        assertEquals(itemId, bookingResponseDto.getItem().getId());
        assertEquals(bookerId, bookingResponseDto.getBooker().getId());

        verify(itemService, times(1)).getById(itemId);
        verify(userService, times(1)).getById(bookerId);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verifyNoMoreInteractions(userService, itemService, bookingRepository);
    }

    @Test
    @DisplayName("Should throw an exception when the item is not available")
    void addBookingWhenItemIsNotAvailableThenThrowException() {
        long bookerId = 1L;
        long itemId = 1L;

        User booker = new User();
        booker.setId(bookerId);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item(itemId, "name", "description", false, owner, null);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );

        when(itemService.getById(itemId)).thenReturn(item);
        when(userService.getById(bookerId)).thenReturn(booker);

        assertThrows(ItemUnavailableException.class, () -> bookingService.add(bookingRequestDto, bookerId));

        verify(itemService, times(1)).getById(itemId);
        verify(userService, times(1)).getById(bookerId);
        verifyNoMoreInteractions(userService, itemService, bookingRepository);
    }

    @Test
    @DisplayName("Should throw an exception when the booker and the owner are the same user")
    void addBookingWhenBookerAndOwnerAreSameUserThenThrowException() {
        long userId = 1L;
        long itemId = 1L;

        User bookerAndOwner = new User();
        bookerAndOwner.setId(userId);

        Item item = new Item(itemId, "name", "description", false, bookerAndOwner, null);

        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );

        when(itemService.getById(itemId)).thenReturn(item);
        when(userService.getById(userId)).thenReturn(bookerAndOwner);

        assertThrows(SameItemOwnerAndBookerIdException.class, () -> bookingService.add(bookingRequestDto, userId));
    }

    @Test
    @DisplayName("Should throw an exception when the user is not found")
    void addBookingWhenUserNotFoundThenThrowException() {
        long bookerId = 1L;
        long itemId = 2L;
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                itemId
        );

        when(userService.getById(bookerId)).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> bookingService.add(bookingRequestDto, bookerId));

        verify(userService, times(1)).getById(bookerId);
        verify(itemService, times(1)).getById(itemId);
        verifyNoMoreInteractions(userService, itemService, bookingRepository);
    }

    @Test
    @DisplayName("Should throw a BookingNotFoundException when the booking with the given ID does not exist")
    void getDtoByIdWhenBookingNotFoundThenThrowBookingNotFoundException() {
        long id = 1L;
        long userId = 2L;

        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getDtoById(id, userId),
                String.format("Booking with id=%d not found", id)
        );

        verify(bookingRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should return the booking DTO when the user is either the booker or the owner")
    void getDtoByIdWhenUserIsBookerOrOwner() {
        long bookingId = 1L;
        long ownerId = 2L;
        long bookerId = 3L;
        long itemId = 4L;

        User owner = new User();
        owner.setId(ownerId);
        Item item = new Item(itemId, "name", "description", true, owner, null);

        User booker = new User();
        booker.setId(bookerId);

        Booking booking = new Booking(
                bookingId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                booker,
                item,
                BookingStatus.WAITING
        );

        BookingResponseDto bookingResponseDto = BookingMapper.toBookingResponseDto(
                booking,
                UserMapper.toUserDto(booker),
                ItemMapper.toItemDto(item)
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Assertions.assertEquals(bookingResponseDto, bookingService.getDtoById(bookingId, ownerId));
    }

    @Test
    @DisplayName("Should throw an UnauthorizedException when the user is neither the booker nor the owner")
    void getDtoByIdWhenUserIsNotBookerOrOwnerThenThrowUnauthorizedException() {
        long bookingId = 1L;
        long ownerId = 2L;
        long bookerId = 3L;
        long itemId = 4L;
        long notBookerOrOwnerId = 5L;

        User owner = new User();
        owner.setId(ownerId);
        Item item = new Item(itemId, "name", "description", true, owner, null);

        User booker = new User();
        booker.setId(bookerId);

        Booking booking = new Booking(
                bookingId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                booker,
                item,
                BookingStatus.WAITING
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(UnauthorizedException.class, () -> bookingService.getDtoById(bookingId, notBookerOrOwnerId));
    }
}