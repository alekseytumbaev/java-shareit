package ru.practicum.shareit.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestResponseDto {
    private long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;
}
