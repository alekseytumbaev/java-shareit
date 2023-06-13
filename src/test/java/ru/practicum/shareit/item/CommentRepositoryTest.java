package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase
public class CommentRepositoryTest {
    private final TestEntityManager testEntityManager;

    private final CommentRepository commentRepo;

    private Item item;

    @Autowired
    public CommentRepositoryTest(TestEntityManager testEntityManager, CommentRepository commentRepo) {
        this.testEntityManager = testEntityManager;
        this.commentRepo = commentRepo;
    }

    @BeforeEach
    public void setUp() {
        User owner = testEntityManager.persist(new User(0, "owner", "owner@mail.com"));
        item = testEntityManager.persist(new Item(0, "item", "description", true, owner, null));
        User booker = testEntityManager.persist(new User(0, "booker", "booker@mail.com"));
        LocalDateTime now = LocalDateTime.now();
        testEntityManager.persist(
                new Booking(0, now.minusDays(2), now.minusDays(1), booker, item, BookingStatus.APPROVED)
        );
        testEntityManager.persist(new Comment(0, "comment1", now, item, booker));
        testEntityManager.persist(new Comment(0, "comment2", now, item, booker));
    }

    @Test
    @DisplayName("Should find all by item id")
    public void findAllByItem_Id() {
        List<Object[]> itemIdToComments = commentRepo.findAllByItem_Id(List.of(item.getId()));
        assertEquals(2, itemIdToComments.size());
        for (Object[] comments : itemIdToComments) {
            assertEquals(2, comments.length);
            assertEquals(item.getId(), comments[0]);
        }
    }

    @Test
    @DisplayName("Should find all by request id as map")
    public void findAllByItem_IdAsMap() {
        Map<Long, List<Comment>> itemIdToComments = commentRepo.findAllByItem_IdAsMap(List.of(item.getId()));
        assertEquals(1, itemIdToComments.size());
        assertEquals(2, itemIdToComments.get(item.getId()).size());
    }
}
