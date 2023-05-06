package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class ItemDto {
    private long id;

    @Size(min = 1, max = 50)
    private String name;
    @Size(min = 1, max = 200)
    private String description;

    private Boolean available;
    private long ownerId;

    /**
    If item was created no the request of another user,
    then in this field will store a link to the corresponding request
     */
    private String request;
}
