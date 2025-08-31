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
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);
        item.setOwner(owner);

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(item.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(2));

        booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
    }

    // --- createBooking Tests ---

    @Test
    void createBooking_whenValid_shouldReturnBooking() throws ValidationException {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking created = bookingService.createBooking(booker.getId(), bookingRequestDto);

        assertNotNull(created);
        assertEquals(Status.WAITING, created.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_whenBookerNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(99L, bookingRequestDto));
    }

    @Test
    void createBooking_whenItemNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(booker.getId(), bookingRequestDto));
    }

    @Test
    void createBooking_whenItemNotAvailable_shouldThrowValidationException() {
        item.setAvailable(false);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(booker.getId(), bookingRequestDto));
    }

    @Test
    void createBooking_whenOwnerBooksOwnItem_shouldThrowNotFoundException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(owner.getId(), bookingRequestDto));
    }

    @Test
    void createBooking_whenInvalidDates_shouldThrowValidationException() {
        bookingRequestDto.setStart(LocalDateTime.now().plusHours(2));
        bookingRequestDto.setEnd(LocalDateTime.now().plusHours(1));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.createBooking(booker.getId(), bookingRequestDto));
    }

    // --- approveBooking Tests ---

    @Test
    void approveBooking_whenApproved_shouldSetStatusToApproved() throws ValidationException {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking approvedBooking = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertEquals(Status.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void approveBooking_whenRejected_shouldSetStatusToRejected() throws ValidationException {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking rejectedBooking = bookingService.approveBooking(owner.getId(), booking.getId(), false);

        assertEquals(Status.REJECTED, rejectedBooking.getStatus());
    }

    @Test
    void approveBooking_whenNotOwner_shouldThrowUserException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(UserException.class, () -> bookingService.approveBooking(booker.getId(), booking.getId(), true));
    }

    @Test
    void approveBooking_whenBookingNotFound_shouldThrowNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(owner.getId(), 99L, true));
    }

    @Test
    void approveBooking_whenAlreadyApproved_shouldThrowValidationException() {
        booking.setStatus(Status.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(ValidationException.class, () -> bookingService.approveBooking(owner.getId(), booking.getId(), true));
    }

    // --- getBookingById Tests ---

    @Test
    void getBookingById_whenOwnerOrBooker_shouldReturnBooking() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertNotNull(bookingService.getBookingById(owner.getId(), booking.getId()));
        assertNotNull(bookingService.getBookingById(booker.getId(), booking.getId()));
    }

    @Test
    void getBookingById_whenNotOwnerOrBooker_shouldThrowNotFoundException() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(99L, booking.getId()));
    }

    @Test
    void getBookingById_whenBookingNotFound_shouldThrowNotFoundException() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(owner.getId(), 99L));
    }

    // --- getBookingsByUser Tests ---

    @Test
    void getBookingsByUser_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByUser(99L, "ALL"));
    }

    @Test
    void getBookingsByUser_whenStateIsInvalid_shouldThrowValidationException() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        assertThrows(ValidationException.class, () -> bookingService.getBookingsByUser(booker.getId(), "INVALID_STATE"));
    }

    private void testGetBookingsByUserWithState(String state, Runnable verifier) throws ValidationException {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        bookingService.getBookingsByUser(booker.getId(), state);
        verifier.run();
    }

    @Test
    void getBookingsByUser_whenAll_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByUserWithState("ALL", () -> verify(bookingRepository).findByBookerId(eq(booker.getId()), any(Sort.class)));
    }

    @Test
    void getBookingsByUser_whenCurrent_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByUserWithState("CURRENT", () -> verify(bookingRepository).findByBookerIdAndStartIsBeforeAndEndIsAfter(eq(booker.getId()), any(), any(), any(Sort.class)));
    }

    @Test
    void getBookingsByUser_whenPast_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByUserWithState("PAST", () -> verify(bookingRepository).findByBookerIdAndEndIsBefore(eq(booker.getId()), any(), any(Sort.class)));
    }

    @Test
    void getBookingsByUser_whenFuture_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByUserWithState("FUTURE", () -> verify(bookingRepository).findByBookerIdAndStartIsAfter(eq(booker.getId()), any(), any(Sort.class)));
    }

    @Test
    void getBookingsByUser_whenWaiting_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByUserWithState("WAITING", () -> verify(bookingRepository).findByBookerIdAndStatus(eq(booker.getId()), eq(Status.WAITING), any(Sort.class)));
    }

    @Test
    void getBookingsByUser_whenRejected_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByUserWithState("REJECTED", () -> verify(bookingRepository).findByBookerIdAndStatus(eq(booker.getId()), eq(Status.REJECTED), any(Sort.class)));
    }

    // --- getBookingsByOwner Tests ---

    @Test
    void getBookingsByOwner_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByOwner(99L, "ALL"));
    }

    @Test
    void getBookingsByOwner_whenStateIsInvalid_shouldThrowValidationException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        assertThrows(ValidationException.class, () -> bookingService.getBookingsByOwner(owner.getId(), "INVALID_STATE"));
    }

    private void testGetBookingsByOwnerWithState(String state, Runnable verifier) throws ValidationException {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        bookingService.getBookingsByOwner(owner.getId(), state);
        verifier.run();
    }

    @Test
    void getBookingsByOwner_whenAll_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByOwnerWithState("ALL", () -> verify(bookingRepository).findByItemOwnerId(eq(owner.getId()), any(Sort.class)));
    }

    @Test
    void getBookingsByOwner_whenCurrent_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByOwnerWithState("CURRENT", () -> verify(bookingRepository).findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(eq(owner.getId()), any(), any(), any(Sort.class)));
    }

    @Test
    void getBookingsByOwner_whenPast_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByOwnerWithState("PAST", () -> verify(bookingRepository).findByItemOwnerIdAndEndIsBefore(eq(owner.getId()), any(), any(Sort.class)));
    }

    @Test
    void getBookingsByOwner_whenFuture_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByOwnerWithState("FUTURE", () -> verify(bookingRepository).findByItemOwnerIdAndStartIsAfter(eq(owner.getId()), any(), any(Sort.class)));
    }

    @Test
    void getBookingsByOwner_whenWaiting_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByOwnerWithState("WAITING", () -> verify(bookingRepository).findByItemOwnerIdAndStatus(eq(owner.getId()), eq(Status.WAITING), any(Sort.class)));
    }

    @Test
    void getBookingsByOwner_whenRejected_shouldCallCorrectRepositoryMethod() throws ValidationException {
        testGetBookingsByOwnerWithState("REJECTED", () -> verify(bookingRepository).findByItemOwnerIdAndStatus(eq(owner.getId()), eq(Status.REJECTED), any(Sort.class)));
    }
}

