package ru.practicum.shareit.booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SimpleBookingDtoResponse {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long bookerId;
    private long itemId;
    private BookingStatus status;
}
