package ru.practicum.shareit.server.booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.server.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SimpleBookingResponseDto {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private long bookerId;
    private long itemId;
    private BookingStatus status;
}
