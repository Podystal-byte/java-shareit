package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public Booking createBooking(Long userId, BookingRequestDto bookingRequestDto) throws ValidationException {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена."));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования.");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь.");
        }

        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart()) || bookingRequestDto.getEnd().isEqual(bookingRequestDto.getStart())) {
            throw new ValidationException("Некорректные даты бронирования.");
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setStatus(Status.WAITING);

        log.info("Создано новое бронирование для вещи {} пользователем {}.", item.getId(), userId);
        return bookingRepository.save(booking);
    }

    public Booking approveBooking(Long userId, Long bookingId, Boolean approved) throws ValidationException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено."));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new UserException("Только владелец вещи может подтверждать бронирование.");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidationException("Статус бронирования уже был изменён.");
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        log.info("Бронирование с id {} было {} владельцем {}.", bookingId, approved ? "подтверждено" : "отклонено", userId);
        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено."));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Получить данные о бронировании может только его автор или владелец вещи.");
        }

        return booking;
    }

    public List<Booking> getBookingsByUser(Long userId, String state) throws ValidationException {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByBookerId(userId, sort);
            case "CURRENT":
                return bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, sort);
            case "PAST":
                return bookingRepository.findByBookerIdAndEndIsBefore(userId, now, sort);
            case "FUTURE":
                return bookingRepository.findByBookerIdAndStartIsAfter(userId, now, sort);
            case "WAITING":
                return bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, sort);
            case "REJECTED":
                return bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, sort);
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    public List<Booking> getBookingsByOwner(Long userId, String state) throws ValidationException {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByItemOwnerId(userId, sort);
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, sort);
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, now, sort);
            case "FUTURE":
                return bookingRepository.findByItemOwnerIdAndStartIsAfter(userId, now, sort);
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING, sort);
            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED, sort);
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }
}