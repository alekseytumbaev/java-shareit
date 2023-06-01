package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_generator")
    @SequenceGenerator(name = "booking_generator", sequenceName = "booking_seq")
    @Column(name = "booking_id")
    private long id;

    @Column(name = "booking_start")
    private LocalDateTime start;

    @Column(name = "booking_end")
    private LocalDateTime end;

    @ManyToOne(optional = false)
    private User booker;

    @ManyToOne(optional = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
