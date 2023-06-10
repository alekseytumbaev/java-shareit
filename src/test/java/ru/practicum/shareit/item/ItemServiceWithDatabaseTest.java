package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.model.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.dto.ItemDto;
import ru.practicum.shareit.item.model.dto.ItemWithBookingsResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class ItemServiceWithDatabaseTest {
    private final EntityManager entityManager;

    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;

    @Test
    @DisplayName("Should add comment when commenter booked item and the booking had ended before the comment was written")
     void addComment() throws InterruptedException {
        UserDto owner = userService.add(new UserDto(0, "owner", "email@mail.com"));
        ItemDto item = itemService.add(
                new ItemDto(0, "item", "description", true, owner.getId(), 0)
        );

        UserDto bookerDto = userService.add(new UserDto(0, "booker", "email@mail.ru"));
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                LocalDateTime.now().plus(5, ChronoUnit.MILLIS),
                LocalDateTime.now().plus(10, ChronoUnit.MILLIS),
                item.getId()
        );
        BookingResponseDto bookingResponseDto = bookingService.add(
                bookingRequestDto,
                bookerDto.getId()
        );
        bookingService.changeStatus(bookingResponseDto.getId(), item.getOwnerId(), true);
        Thread.sleep(10);

        CommentResponseDto commentResponseDto = itemService.addComment(
                new CommentRequestDto("comment"), item.getId(), bookerDto.getId()
        );

        Query query = entityManager.createQuery(
                "SELECT c FROM Comment c where c.item.id = :itemId"
        );
        query.setParameter("itemId", item.getId());
        List<Comment> comments = query.getResultList();
        assertEquals(1, comments.size());
        assertEquals(commentResponseDto.getId(), comments.get(0).getId());
    }

    @Test
    @DisplayName("Should add itemn")
    void add() {
        UserDto owner = userService.add(new UserDto(0, "owner", "email@mail.com"));
        ItemDto item = itemService.add(
                new ItemDto(0, "item", "description", true, owner.getId(), 0)
        );

        assertEquals(item.getId(), itemService.getDtoById(item.getId(), owner.getId()).getId());
    }

    @Test
    @DisplayName("Should get dto by owner id with last and next bookings")
    void getDtoByOwnerId() {
        UserDto owner = userService.add(new UserDto(0, "owner", "email@mail.com"));
        ItemDto itemDto = itemService.add(new ItemDto(0, "item", "description", true, owner.getId(), 0));

        UserDto booker = userService.add(new UserDto(0, "booker", "email@mail.ru"));

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                now.minusDays(2),
                now.minusDays(1),
                itemDto.getId()
        );
        BookingResponseDto lastBookingDto = bookingService.add(bookingRequestDto, booker.getId());
        BookingResponseDto lastBookingApproved = bookingService.changeStatus(
                lastBookingDto.getId(), itemDto.getOwnerId(), true
        );

        bookingRequestDto = new BookingRequestDto(
                now.plusDays(1),
                now.plusDays(2),
                itemDto.getId()
        );
        BookingResponseDto nextBookingDto = bookingService.add(bookingRequestDto, booker.getId());
        BookingResponseDto nextBookingApproved = bookingService.changeStatus(
                nextBookingDto.getId(), itemDto.getOwnerId(), true
        );

        CommentResponseDto commentResponseDto = itemService.addComment(
                new CommentRequestDto("comment"), itemDto.getId(), booker.getId()
        );
        ItemWithBookingsResponseDto itemResponseDto = itemService.getDtoById(itemDto.getId(), owner.getId());

        assertEquals(itemResponseDto.getOwnerId(), owner.getId());
        assertEquals(lastBookingApproved.getId(), itemResponseDto.getLastBooking().getId());
        assertEquals(nextBookingApproved.getId(), itemResponseDto.getNextBooking().getId());
        assertEquals(commentResponseDto.getId(), itemResponseDto.getComments().get(0).getId());
    }

    @Test
    @DisplayName("Should get all dtos by owner id with last and next bookings and comments")
    void getAllDtosByOwnerId() {
        UserDto owner = userService.add(new UserDto(0, "owner", "email@mail.com"));
        ItemDto itemDto1 = itemService.add(new ItemDto(0, "item", "description", true, owner.getId(), 0));

        UserDto booker = userService.add(new UserDto(0, "booker", "email@mail.ru"));

        LocalDateTime now = LocalDateTime.now();
        BookingRequestDto bookingRequestDto = new BookingRequestDto(
                now.minusDays(2),
                now.minusDays(1),
                itemDto1.getId()
        );
        BookingResponseDto lastBookingDto = bookingService.add(bookingRequestDto, booker.getId());
        BookingResponseDto lastBookingApproved = bookingService.changeStatus(
                lastBookingDto.getId(), itemDto1.getOwnerId(), true
        );

        bookingRequestDto = new BookingRequestDto(
                now.plusDays(1),
                now.plusDays(2),
                itemDto1.getId()
        );
        BookingResponseDto nextBookingDto = bookingService.add(bookingRequestDto, booker.getId());
        BookingResponseDto nextBookingApproved = bookingService.changeStatus(
                nextBookingDto.getId(), itemDto1.getOwnerId(), true
        );

        CommentResponseDto commentResponseDto = itemService.addComment(
                new CommentRequestDto("comment"), itemDto1.getId(), booker.getId()
        );

        ItemDto itemDto2 = itemService.add(
                new ItemDto(0, "item", "description", true, owner.getId(), 0)
        );

        Collection<ItemWithBookingsResponseDto> items = itemService.getAllByOwnerId(owner.getId(), 0, 10);
        assertEquals(2, items.size());

        Iterator<ItemWithBookingsResponseDto> iterator = items.iterator();

        ItemWithBookingsResponseDto itemResponseDto1 = iterator.next();
        assertEquals(itemDto1.getId(), itemResponseDto1.getId());
        assertEquals(lastBookingApproved.getId(), itemResponseDto1.getLastBooking().getId());
        assertEquals(nextBookingApproved.getId(), itemResponseDto1.getNextBooking().getId());
        assertEquals(commentResponseDto.getId(), itemResponseDto1.getComments().get(0).getId());

        ItemWithBookingsResponseDto itemResponseDto2 = iterator.next();
        assertEquals(itemDto2.getId(), itemResponseDto2.getId());
    }

    @Test
    @DisplayName("Should search by name or descripiton")
    void searchByNameOrDescription() {
        UserDto owner = userService.add(new UserDto(0, "owner", "email@mail.com"));
        long ownerId = owner.getId();
        itemService.add(new ItemDto(0, "item1", "description1", true, ownerId, 0));
        itemService.add(new ItemDto(0, "item2", "item description2", true, ownerId, 0));
        itemService.add(new ItemDto(0, "asfasf", "asdfasdf", true, ownerId, 0));

        Collection<ItemDto> items = itemService.searchByNameOrDescription("item", 0, 10);
        assertEquals(2, items.size());
        Iterator<ItemDto> iterator = items.iterator();
        assertTrue(iterator.next().getName().contains("item"));
        assertTrue(iterator.next().getDescription().contains("item"));
    }

    @Test
    @DisplayName("Should update item, replacing null values with existing")
    void update() {
        UserDto owner = userService.add(new UserDto(0, "owner", "email@mail.com"));
        long ownerId = owner.getId();
        ItemDto addedItem = itemService.add(new ItemDto(0, "item", "description", true, ownerId, 0));

        ItemDto itemToUpdate = new ItemDto(
                addedItem.getId(),
                "updated item",
                null,
                true,
                ownerId,
                0
        );
        itemService.update(itemToUpdate);
        ItemWithBookingsResponseDto updatedItem = itemService.getDtoById(addedItem.getId(), ownerId);
        assertEquals("updated item", updatedItem.getName());
        assertEquals(addedItem.getDescription(), updatedItem.getDescription());
        assertEquals(ownerId, updatedItem.getOwnerId());
    }
}
