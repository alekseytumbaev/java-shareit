package ru.practicum.shareit.item.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.dto.SimpleBookingResponseDto;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemWithBookingsResponseDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long ownerId;
    private SimpleBookingResponseDto lastBooking;
    private SimpleBookingResponseDto nextBooking;
    private List<CommentResponseDto> comments;
}
