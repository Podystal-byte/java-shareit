package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Data
public class User {
    private int id;
    private String name;
    @Email
    private String email;

    private List<ItemDto> items;
}
