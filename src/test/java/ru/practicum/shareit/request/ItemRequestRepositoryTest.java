package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase
public class ItemRequestRepositoryTest {
    private final TestEntityManager testEntityManager;

    private final ItemRequestRepository itemRequestRepo;

    @Autowired
    public ItemRequestRepositoryTest(TestEntityManager testEntityManager, ItemRequestRepository itemRequestRepo) {
        this.testEntityManager = testEntityManager;
        this.itemRequestRepo = itemRequestRepo;
    }

    @Test
    @DisplayName("Should find all except author's")
    public void findAllExceptAuthor_Id() {
        User requestAuthor = testEntityManager.persist(new User(0, "owner", "owner@mail.com"));
        User notAuthor = testEntityManager.persist(new User(0, "notAuthor", "notAuthor@mail.com"));
        itemRequestRepo.saveAll(List.of(
                new ItemRequest(0, "description", LocalDateTime.now(), requestAuthor),
                new ItemRequest(0, "description", LocalDateTime.now(), notAuthor),
                new ItemRequest(0, "description", LocalDateTime.now(), notAuthor)
        ));
        Page<ItemRequest> page = itemRequestRepo.findAllExceptAuthor_Id(PageRequest.of(0, 3), requestAuthor.getId());
        assertEquals(2, page.getTotalElements());
        assertEquals(notAuthor.getId(), page.getContent().get(0).getAuthor().getId());
        assertEquals(notAuthor.getId(), page.getContent().get(1).getAuthor().getId());
    }
}
