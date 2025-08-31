package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {BookingMapperImpl.class})
class BookingMapperTest {

    @Autowired
    private BookingMapper bookingMapper;

    @Test
    void toBookingDto() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");

        User booker = new User();
        booker.setId(2L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusHours(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.APPROVED);

        BookingDto bookingDto = bookingMapper.toBookingDto(booking);

        assertNotNull(bookingDto);
        assertEquals(booking.getId().intValue(), bookingDto.getId());
        assertEquals(booking.getStart(), bookingDto.getStart());
        assertEquals(booking.getEnd(), bookingDto.getEnd());
        assertEquals(booking.getStatus(), bookingDto.getStatus());
        assertNotNull(bookingDto.getItem());
        assertNotNull(bookingDto.getBooker());
    }

    @Test
    void toBookingDto_whenBookingIsNull_shouldReturnNull() {
        assertNull(bookingMapper.toBookingDto(null));
    }

    @Test
    void toBooking() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);

        UserDto userDto = new UserDto();
        userDto.setId(2L);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1);
        bookingDto.setStart(LocalDateTime.now());
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingDto.setItem(itemDto);
        bookingDto.setBooker(userDto);
        bookingDto.setStatus(Status.WAITING);

        Booking booking = bookingMapper.toBooking(bookingDto);

        assertNotNull(booking);
        assertEquals(bookingDto.getId(), booking.getId().intValue());
        assertEquals(bookingDto.getStart(), booking.getStart());
        assertEquals(bookingDto.getEnd(), booking.getEnd());
        assertEquals(bookingDto.getStatus(), booking.getStatus());
        assertNotNull(booking.getItem());
        assertNotNull(booking.getBooker());
    }

    @Test
    void toBooking_whenDtoIsNull_shouldReturnNull() {
        assertNull(bookingMapper.toBooking(null));
    }
}
