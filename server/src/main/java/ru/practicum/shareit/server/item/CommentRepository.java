package ru.practicum.shareit.server.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.server.item.model.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(
            "select c.item.id, c from Comment c " +
                    "where c.item.id in :itemIds"
    )
    List<Object[]> findAllByItem_Id(Iterable<Long> itemIds);

    default Map<Long, List<Comment>> findAllByItem_IdAsMap(Iterable<Long> itemIds) {
        List<Object[]> results = findAllByItem_Id(itemIds);
        Map<Long, List<Comment>> itemIdToComments = new HashMap<>();
        for (Object[] result : results) {
            Long itemId = (Long) result[0];
            Comment comment = (Comment) result[1];
            itemIdToComments.putIfAbsent(itemId, new ArrayList<>());
            itemIdToComments.get(itemId).add(comment);
        }
        return itemIdToComments;
    }
}
