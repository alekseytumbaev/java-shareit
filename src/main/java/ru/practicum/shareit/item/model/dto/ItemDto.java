package ru.practicum.shareit.item.model.dto;

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
    private long requestId;
}
