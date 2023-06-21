package ru.practicum.shareit.server.booking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequestDto {
    private LocalDateTime start;
    private LocalDateTime end;
    private long itemId;
}
