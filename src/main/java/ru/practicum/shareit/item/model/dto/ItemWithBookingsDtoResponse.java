package ru.practicum.shareit.item.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.dto.SimpleBookingDtoResponse;

@Data
@AllArgsConstructor
public class ItemWithBookingsDtoResponse {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private long ownerId;
    private SimpleBookingDtoResponse lastBooking;
    private SimpleBookingDtoResponse nextBooking;

    /**
     If item was created on the request of another user,
     then in this field will store a link to the corresponding request
     */
    private String request;
}
