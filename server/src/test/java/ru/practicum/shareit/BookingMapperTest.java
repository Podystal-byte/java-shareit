package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BookingMapperTest {

    private BookingMapperImpl bookingMapper;

    @BeforeEach
    public void setUp() {
        bookingMapper = new BookingMapperImpl();
    }

    @Test
    public void toBookingDto_whenValidBooking_thenCorrectlyMapped() {
        // Given
        User booker = new User();
        booker.setId(1L);
        booker.setName("test user");
        booker.setEmail("test@test.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("test item");
        item.setDescription("test description");
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));
        booking.setStatus(Status.APPROVED);
        booking.setBooker(booker);
        booking.setItem(item);

        // When
        BookingDto bookingDto = bookingMapper.toBookingDto(booking);

        // Then
        assertNotNull(bookingDto);
        assertEquals(booking.getId().intValue(), bookingDto.getId());
        assertEquals(booking.getStart(), bookingDto.getStart());
        assertEquals(booking.getEnd(), bookingDto.getEnd());
        assertEquals(booking.getStatus(), bookingDto.getStatus());
        assertEquals(booker.getId(), bookingDto.getBooker().getId());
        assertEquals(item.getId(), bookingDto.getItem().getId());
    }

    @Test
    public void toBookingDto_whenNullBooking_thenReturnsNull() {
        // When
        BookingDto bookingDto = bookingMapper.toBookingDto(null);

        // Then
        assertNull(bookingDto);
    }

    @Test
    public void toBooking_whenValidBookingDto_thenCorrectlyMapped() {
        // Given
        UserDto bookerDto = new UserDto();
        bookerDto.setId(1L);
        bookerDto.setName("test user");
        bookerDto.setEmail("test@test.com");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("test item");
        itemDto.setDescription("test description");
        itemDto.setAvailable(true);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1);
        bookingDto.setStart(LocalDateTime.now().minusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingDto.setStatus(Status.APPROVED);
        bookingDto.setBooker(bookerDto);
        bookingDto.setItem(itemDto);

        // When
        Booking booking = bookingMapper.toBooking(bookingDto);

        // Then
        assertNotNull(booking);
        assertEquals((long) bookingDto.getId(), booking.getId());
        assertEquals(bookingDto.getStart(), booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd());
        assertEquals(bookingDto.getStatus(), booking.getStatus());
        assertEquals(bookerDto.getId(), booking.getBooker().getId());
        assertEquals(itemDto.getId(), booking.getItem().getId());
    }

    @Test
    public void toBooking_whenNullBookingDto_thenReturnsNull() {
        // When
        Booking booking = bookingMapper.toBooking(null);

        // Then
        assertNull(booking);
    }
}