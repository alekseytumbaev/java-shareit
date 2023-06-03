package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Collection<Item> findAllByOwner_Id(long ownerId);

    @Query(
            "select i from Item i " +
                    "where i.available = true and (" +
                    "(i.name) like upper(concat('%',:text,'%')) or " +
                    "upper(i.description) like upper(concat('%',:text,'%')))"
    )
    Collection<Item> searchByNameOrDescription(String text);
}
