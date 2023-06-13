package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.*;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(
            "select b from Booking b " +
                    "where current_timestamp between b.start and b.end and " +
                    "b.booker.id = :bookerId"
    )
    Page<Booking> findAllCurrentByBooker_Id(long bookerId, PageRequest pageRequest);

    @Query(
            "select b from Booking b " +
                    "where b.end < current_timestamp and " +
                    "b.status = 'APPROVED' and " +
                    "b.booker.id = :bookerId"
    )
    Page<Booking> findAllPastByBooker_Id(long bookerId, PageRequest pageRequest);

    @Query(
            "select b from Booking b " +
                    "where b.start > current_timestamp and " +
                    "(b.status = 'APPROVED' or b.status = 'WAITING') and " +
                    "b.booker.id = :bookerId"
    )
    Page<Booking> findAllFutureByBooker_Id(long bookerId, PageRequest pageRequest);

    Page<Booking> findAllByStatusAndBooker_Id(BookingStatus bookingStatus, long bookerId, PageRequest pageRequest);

    Page<Booking> findAllByBooker_Id(long bookerId, PageRequest pageRequest);

    @Query(
            "select b from Booking b " +
                    "where current_timestamp between b.start and b.end and " +
                    "b.item.owner.id = :itemOwnerId " +
                    "order by b.start desc"
    )
    Page<Booking> findAllCurrentByItem_Owner_IdOrderByStartDesc(long itemOwnerId, PageRequest pageRequest);

    @Query(
            "select b from Booking b " +
                    "where b.end < current_timestamp and " +
                    "b.status = 'APPROVED' and " +
                    "b.item.owner.id = :itemOwnerId " +
                    "order by b.start desc"
    )
    Page<Booking> findAllPastByItem_Owner_IdOrderByStartDesc(long itemOwnerId, PageRequest pageRequest);

    @Query(
            "select b from Booking b " +
                    "where b.start > current_timestamp and " +
                    "(b.status = 'APPROVED' or b.status = 'WAITING') and " +
                    "b.item.owner.id = :itemOwnerId " +
                    "order by b.start desc"
    )
    Page<Booking> findAllFutureByItem_Owner_IdOrderByStartDesc(long itemOwnerId, PageRequest pageRequest);

    Page<Booking> findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus bookingStatus, long itemOwnerId,
                                                                        PageRequest pageRequest);

    Page<Booking> findAllByItem_Owner_IdOrderByStartDesc(long itemOwnerId, PageRequest pageRequest);

    Collection<Booking> findAllByItem_IdAndBooker_Id(long itemId, long bookerId);

    @Query(
            "select b.item.id, b from Booking b " +
                    "where b.item.id in :itemIds"
    )
    List<Object[]> findAllByItem_Id(Iterable<Long> itemIds);

    default Map<Long, List<Booking>> findAllByItem_IdAsMap(Iterable<Long> itemIds) {
        List<Object[]> results = findAllByItem_Id(itemIds);
        Map<Long, List<Booking>> itemIdToBookings = new HashMap<>();
        for (Object[] result : results) {
            Long itemId = (Long) result[0];
            Booking booking = (Booking) result[1];
            itemIdToBookings.putIfAbsent(itemId, new ArrayList<>());
            itemIdToBookings.get(itemId).add(booking);
        }
        return itemIdToBookings;
    }
}
