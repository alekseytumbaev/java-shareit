package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase
public class BookingRepositoryTest {
    private final TestEntityManager testEntityManager;

    private final BookingRepository bookingRepo;

    private User owner;
    private User booker;
    private Item item;
    private Booking currentBooking;
    private Booking pastBooking;
    private Booking futureBooking;

    @Autowired
    public BookingRepositoryTest(TestEntityManager testEntityManager, BookingRepository bookingRepo) {
        this.testEntityManager = testEntityManager;
        this.bookingRepo = bookingRepo;
    }

    @BeforeEach
    public void setUp() {
        owner = testEntityManager.persist(new User(0, "owner", "owner@mail.com"));
        booker = testEntityManager.persist(new User(0, "booker", "booker@mail.com"));
        item = testEntityManager.persist(new Item(0, "item", "item description", true, owner, null));
        LocalDateTime now = LocalDateTime.now();
        currentBooking = testEntityManager.persist(
                new Booking(0, now.minusDays(1), now.plusDays(1), booker, item, BookingStatus.APPROVED)
        );
        pastBooking = testEntityManager.persist(
                new Booking(0, now.minusDays(10), now.minusDays(9), booker, item, BookingStatus.APPROVED)
        );
        futureBooking = testEntityManager.persist(
                new Booking(0, now.plusDays(3), now.plusDays(4), booker, item, BookingStatus.APPROVED)
        );
    }

    @Test
    @DisplayName("Should find all current bookings by booker id")
    public void findAllCurrentByBooker_Id() {
        Page<Booking> bookings = bookingRepo.findAllCurrentByBooker_Id(booker.getId(), PageRequest.of(0, 1));
        assertEquals(currentBooking, bookings.getContent().get(0));
    }

    @Test
    @DisplayName("Should find all past bookings by booker id")
    public void findAllPastByBooker_Id() {
        Page<Booking> bookings = bookingRepo.findAllPastByBooker_Id(booker.getId(), PageRequest.of(0, 1));
        assertEquals(pastBooking, bookings.getContent().get(0));
    }

    @Test
    @DisplayName("Should find all future bookings by booker id")
    public void findAllFutureByBooker_Id() {
        Page<Booking> bookings = bookingRepo.findAllFutureByBooker_Id(booker.getId(), PageRequest.of(0, 1));
        assertEquals(futureBooking, bookings.getContent().get(0));
    }

    @Test
    @DisplayName("Should find all current bookings by item owner id")
    public void findAllCurrentByItem_Owner_IdOrderByStartDesc() {
        Page<Booking> bookings = bookingRepo
                .findAllCurrentByItem_Owner_IdOrderByStartDesc(owner.getId(), PageRequest.of(0, 1));
        assertEquals(currentBooking, bookings.getContent().get(0));
    }

    @Test
    @DisplayName("Should find all past bookings by item owner id")
    public void findAllPastByItem_Owner_IdOrderByStartDesc() {
        Page<Booking> bookings = bookingRepo
                .findAllPastByItem_Owner_IdOrderByStartDesc(owner.getId(), PageRequest.of(0, 1));
        assertEquals(pastBooking, bookings.getContent().get(0));
    }

    @Test
    @DisplayName("Should find all future bookings by item owner id")
    public void findAllFutureByItem_Owner_IdOrderByStartDesc() {
        Page<Booking> bookings = bookingRepo
                .findAllFutureByItem_Owner_IdOrderByStartDesc(owner.getId(), PageRequest.of(0, 1));
        assertEquals(futureBooking, bookings.getContent().get(0));
    }

    @Test
    @DisplayName("Should find all by item id")
    public void findAllByItem_Id() {
        List<Object[]> itemIdToBookings = bookingRepo.findAllByItem_Id(List.of(item.getId()));
        assertEquals(3, itemIdToBookings.size());
        for (Object[] bookings : itemIdToBookings) {
            assertEquals(2, bookings.length);
            assertEquals(item.getId(), bookings[0]);
        }
    }

    @Test
    @DisplayName("Should find all by item id as map")
    public void findAllByItem_IdAsMap() {
        Map<Long, List<Booking>> itemIdToBookings = bookingRepo.findAllByItem_IdAsMap(List.of(item.getId()));
        assertEquals(1, itemIdToBookings.size());
        assertEquals(3, itemIdToBookings.get(item.getId()).size());
    }
}
