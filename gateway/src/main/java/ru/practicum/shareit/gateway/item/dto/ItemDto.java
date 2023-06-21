package ru.practicum.shareit.gateway.item.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.gateway.util.constraint_group.Creation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private long id;

    @NotNull(groups = Creation.class)
    @Size(min = 1, max = 50)
    private String name;

    @NotNull(groups = Creation.class)
    @Size(min = 1, max = 200)
    private String description;

    @NotNull(groups = Creation.class)
    private Boolean available;

    @JsonIgnore
    private long ownerId;

    private long requestId;
}
