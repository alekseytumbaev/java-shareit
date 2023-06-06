package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.*;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(
            "select b from Booking b " +
                    "where current_timestamp between b.start and b.end and " +
                    "b.booker.id = :bookerId " +
                    "order by b.start desc"
    )
    Collection<Booking> findAllCurrentByBooker_IdOrderByStartDesc(long bookerId);

    @Query(
            "select b from Booking b " +
                    "where b.end < current_timestamp and " +
                    "b.status = 'APPROVED' and " +
                    "b.booker.id = :bookerId " +
                    "order by b.start desc"
    )
    Collection<Booking> findAllPastByBooker_IdOrderByStartDesc(long bookerId);

    @Query(
            "select b from Booking b " +
                    "where b.start > current_timestamp and " +
                    "(b.status = 'APPROVED' or b.status = 'WAITING') and " +
                    "b.booker.id = :bookerId " +
                    "order by b.start desc"
    )
    Collection<Booking> findAllFutureByBooker_IdOrderByStartDesc(long bookerId);

    Collection<Booking> findAllByStatusAndBooker_IdOrderByStartDesc(BookingStatus bookingStatus, long bookerId);

    Collection<Booking> findAllByBooker_IdOrderByStartDesc(long bookerId);

    @Query(
            "select b from Booking b " +
                    "where current_timestamp between b.start and b.end and " +
                    "b.item.owner.id = :itemOwnerId " +
                    "order by b.start desc"
    )
    Collection<Booking> findAllCurrentByItem_Owner_IdOrderByStartDesc(long itemOwnerId);

    @Query(
            "select b from Booking b " +
                    "where b.end < current_timestamp and " +
                    "b.status = 'APPROVED' and " +
                    "b.item.owner.id = :itemOwnerId " +
                    "order by b.start desc"
    )
    Collection<Booking> findAllPastByItem_Owner_IdOrderByStartDesc(long itemOwnerId);

    @Query(
            "select b from Booking b " +
                    "where b.start > current_timestamp and " +
                    "(b.status = 'APPROVED' or b.status = 'WAITING') and " +
                    "b.item.owner.id = :itemOwnerId " +
                    "order by b.start desc"
    )
    Collection<Booking> findAllFutureByItem_Owner_IdOrderByStartDesc(long itemOwnerId);

    Collection<Booking> findAllByStatusAndItem_Owner_IdOrderByStartDesc(BookingStatus bookingStatus, long itemOwnerId);

    Collection<Booking> findAllByItem_Owner_IdOrderByStartDesc(long itemOwnerId);

    Collection<Booking> findAllByItem_IdAndBooker_Id(long itemId, long bookerId);

    @Query(
            "select b.item.id, b from Booking b " +
                    "where b.item.id in :itemIds"
    )
    List<Object[]> findAllByItem_Ids(Iterable<Long> itemIds);

    default Map<Long, List<Booking>> findAllByItem_IdAsMap(Iterable<Long> itemIds) {
        List<Object[]> results = findAllByItem_Ids(itemIds);
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
