package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingService bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingRequestDto bookingRequestDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("owner");
        owner.setEmail("owner@mail.ru");

        booker = new User();
        booker.setId(2L);
        booker.setName("booker");
        booker.setEmail("booker@mail.ru");

        item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(Status.WAITING);

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void createBooking_whenUserAndItemFound_thenSavesBooking() throws ValidationException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking savedBooking = bookingService.createBooking(2L, bookingRequestDto);

        assertNotNull(savedBooking);
        assertEquals(booking.getId(), savedBooking.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_whenUserNotFound_thenThrowsNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(99L, bookingRequestDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenItemNotFound_thenThrowsNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(2L, bookingRequestDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenItemNotAvailable_thenThrowsValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(2L, bookingRequestDto));
    }

    @Test
    void createBooking_whenUserIsOwner_thenThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(1L, bookingRequestDto));
    }

    @Test
    void approveBooking_whenApprovedAndStatusIsWaiting_thenSavesWithApprovedStatus() throws ValidationException {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.approveBooking(1L, 1L, true);

        assertEquals(Status.APPROVED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approveBooking_whenRejectedAndStatusIsWaiting_thenSavesWithRejectedStatus() throws ValidationException {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.approveBooking(1L, 1L, false);

        assertEquals(Status.REJECTED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approveBooking_whenUserIsNotOwner_thenThrowsUserException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(UserException.class, () -> bookingService.approveBooking(99L, 1L, true));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookingById_whenUserIsBooker_thenReturnsBooking() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Booking result = bookingService.getBookingById(2L, 1L);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBookingsByUser_whenStateIsAll_thenReturnsAllBookings() throws ValidationException {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerId(anyLong(), any(Sort.class))).thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getBookingsByUser(2L, "ALL");

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        verify(bookingRepository).findByBookerId(anyLong(), any(Sort.class));
    }

    @Test
    void getBookingsByUser_whenStateIsCurrent_thenReturnsCurrentBookings() throws ValidationException {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getBookingsByUser(2L, "CURRENT");
        assertFalse(bookings.isEmpty());
        verify(bookingRepository).findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getBookingsByUser_whenInvalidState_thenThrowsValidationException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        assertThrows(ValidationException.class, () -> bookingService.getBookingsByUser(2L, "UNKNOWN_STATE"));
    }

    @Test
    void getBookingsByOwner_whenStateIsAll_thenReturnsAllBookings() throws ValidationException {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerId(anyLong(), any(Sort.class))).thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getBookingsByOwner(1L, "ALL");

        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
        verify(bookingRepository).findByItemOwnerId(anyLong(), any(Sort.class));
    }

    @Test
    void getBookingsByOwner_whenStateIsPast_thenReturnsPastBookings() throws ValidationException {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getBookingsByOwner(1L, "PAST");

        assertFalse(bookings.isEmpty());
        verify(bookingRepository).findByItemOwnerIdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(Sort.class));
    }

    @Test
    void getBookingsByOwner_whenInvalidState_thenThrowsValidationException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        assertThrows(ValidationException.class, () -> bookingService.getBookingsByOwner(1L, "INVALID_STATE"));
    }
}