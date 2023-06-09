package ru.practicum.shareit.item.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
