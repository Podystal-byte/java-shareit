package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequestDto {

    @NotNull(message = "Item ID must not be null")
    private Long itemId;

    @NotNull(message = "Start date must not be null")
    private LocalDateTime start;

    @NotNull(message = "End date must not be null")
    private LocalDateTime end;
}