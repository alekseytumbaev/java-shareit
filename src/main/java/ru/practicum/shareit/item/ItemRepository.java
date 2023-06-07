package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Collection<Item> findAllByOwner_Id(long ownerId);

    @Query(
            "select i from Item i " +
                    "where i.available = true and (" +
                    "(i.name) like upper(concat('%',:text,'%')) or " +
                    "upper(i.description) like upper(concat('%',:text,'%')))"
    )
    Collection<Item> searchByNameOrDescription(String text);

    @Query(
            "select i.request.id, i from Item i " +
                    "where i.request.id in :requestIds"
    )
    List<Object[]> findAllByRequest_Id(Iterable<Long> requestIds);

    default Map<Long, List<Item>> findAllByRequest_IdAsMap(Iterable<Long> requestIds) {
        List<Object[]> responses = findAllByRequest_Id(requestIds);
        Map<Long, List<Item>> requestIdToItems = new HashMap<>();
        for (Object[] response : responses) {
            Long requestId = (Long) response[0];
            Item item = (Item) response[1];
            requestIdToItems.putIfAbsent(requestId, new ArrayList<>());
            requestIdToItems.get(requestId).add(item);
        }
        return requestIdToItems;
    }
}
