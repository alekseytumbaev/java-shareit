package ru.practicum.shareit.gateway.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.gateway.util.constraint_group.Creation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class UserDto {
    private long id;

    @NotNull(groups = Creation.class)
    @Size()
    private String name;

    @NotNull(groups = Creation.class)
    @Email
    private String email;
}
