package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @Valid @RequestBody BookingRequestDto bookingRequestDto) throws ValidationException {
        log.info("Запрос на создание бронирования от пользователя {} для вещи {}.", userId, bookingRequestDto.getItemId());
        return bookingService.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public Booking approveBooking(@RequestHeader(USER_ID_HEADER) Long userId,
                                  @PathVariable Long bookingId,
                                  @RequestParam Boolean approved) throws ValidationException, ConflictException {
        log.info("Запрос на {} бронирования {} от пользователя {}.", approved ? "подтверждение" : "отклонение", bookingId, userId);
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking getBookingById(@RequestHeader(USER_ID_HEADER) Long userId,
                                  @PathVariable Long bookingId) {
        log.info("Запрос на получение данных о бронировании {} от пользователя {}.", bookingId, userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<Booking> getBookingsByUser(@RequestHeader(USER_ID_HEADER) Long userId,
                                           @RequestParam(defaultValue = "ALL") String state) throws ValidationException {
        log.info("Запрос на получение бронирований пользователя {} со статусом {}.", userId, state);
        return bookingService.getBookingsByUser(userId, state);
    }

    @GetMapping("/owner")
    public List<Booking> getBookingsByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestParam(defaultValue = "ALL") String state) throws ValidationException {
        log.info("Запрос на получение бронирований для вещей пользователя {} со статусом {}.", userId, state);
        return bookingService.getBookingsByOwner(userId, state);
    }
}