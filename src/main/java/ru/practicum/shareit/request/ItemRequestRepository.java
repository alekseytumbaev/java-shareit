package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByAuthor_IdOrderByCreatedDesc(long authorId);

    @Query (
            "select ir from ItemRequest ir " +
                    "where ir.author.id != :authorId"
    )
    Page<ItemRequest> findAllExceptAuthor_Id(PageRequest pageRequest, long authorId);
}
