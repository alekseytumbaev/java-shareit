package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingDto {
    private long id;

    @FutureOrPresent
    private LocalDateTime start;

    @Future
    private LocalDateTime end;

    private long itemId;

    @NotNull
    private BookingStatus status;
}
