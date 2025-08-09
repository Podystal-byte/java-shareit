package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class BookingDto {
    private int id;
    private LocalDate start;
    private LocalDate end;
    private Item item;
    private Integer idBooker;
    private Status status;
}
