package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Booking {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long itemId;
    private BookingStatus status;
}
