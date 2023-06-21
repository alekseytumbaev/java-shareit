package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase
public class ItemRepositoryTest {
    private final TestEntityManager testEntityManager;

    private final ItemRepository itemRepo;

    private ItemRequest itemRequest;

    @Autowired
    public ItemRepositoryTest(TestEntityManager testEntityManager, ItemRepository itemRepo) {
        this.testEntityManager = testEntityManager;
        this.itemRepo = itemRepo;
    }

    @BeforeEach
    public void setUp() {
        User owner = testEntityManager.persist(new User(0, "owner", "owner@mail.com"));
        User requestAuthor = testEntityManager.persist(new User(0, "requestAuthor", "requestAuthor@mail.com"));
        itemRequest = testEntityManager.persist(new ItemRequest(0, "request description", LocalDateTime.now(), requestAuthor));
        testEntityManager.persist(new Item(0, "item1", "description1", true, owner, itemRequest));
        testEntityManager.persist(new Item(0, "item2", "item description", true, owner, itemRequest));
        testEntityManager.persist(new Item(0, "safdasdf", "sdafsdaf", true, owner, itemRequest));
    }

    @Test
    @DisplayName("Should search by name or description")
    void searchByNameOrDescription() {
        Page<Item> itemPage = itemRepo.searchByNameOrDescription("item", PageRequest.of(0, 3));
        Assertions.assertEquals(2, itemPage.getTotalElements());
        List<Item> items = itemPage.getContent();
        assertTrue(items.get(0).getName().contains("item"));
        assertTrue(items.get(1).getName().contains("item"));
    }

    @Test
    @DisplayName("Should find all by request id")
    public void findAllByRequest_Id() {
        List<Object[]> requestIdToItems = itemRepo.findAllByRequest_Id(List.of(itemRequest.getId()));
        assertEquals(3, requestIdToItems.size());
        for (Object[] items : requestIdToItems) {
            assertEquals(2, items.length);
            assertEquals(itemRequest.getId(), items[0]);
        }
    }

    @Test
    @DisplayName("Should find all by request id as map")
    public void findAllByRequest_IdAsMap() {
        Map<Long, List<Item>> itemIdToBookings = itemRepo.findAllByRequest_IdAsMap(List.of(itemRequest.getId()));
        assertEquals(1, itemIdToBookings.size());
        assertEquals(3, itemIdToBookings.get(itemRequest.getId()).size());
    }
}
