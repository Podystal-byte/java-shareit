package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ItemMapperImpl.class})
class ItemMapperTest {

    @Autowired
    private ItemMapper itemMapper;

    @Test
    void toDto() {
        // Given
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("A powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);

        // When
        ItemDto itemDto = itemMapper.toDto(item);

        // Then
        assertNotNull(itemDto);
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertEquals(item.getOwner().getId().intValue(), itemDto.getOwnerId());
    }

    @Test
    void toDto_whenItemIsNull_shouldReturnNull() {
        assertNull(itemMapper.toDto(null));
    }

    @Test
    void toItemWithBookingsAndCommentsDto() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Saw");
        item.setDescription("A sharp saw");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setBooker(new User());

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setBooker(new User());

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great saw!");
        comment.setAuthor(new User());
        comment.setCreated(LocalDateTime.now());
        List<Comment> comments = Collections.singletonList(comment);

        ItemWithBookingsAndCommentsDto dto = itemMapper.toItemWithBookingsAndCommentsDto(item, lastBooking, nextBooking, comments);

        assertNotNull(dto);
        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
        assertEquals(item.getOwner().getId(), dto.getOwnerId());

        assertNotNull(dto.getLastBooking());
        assertEquals(lastBooking.getId(), dto.getLastBooking().getId());

        assertNotNull(dto.getNextBooking());
        assertEquals(nextBooking.getId(), dto.getNextBooking().getId());

        assertNotNull(dto.getComments());
        assertFalse(dto.getComments().isEmpty());
    }

    @Test
    void toItemWithBookingsAndCommentsDto_whenItemIsNull_shouldReturnNull() {
        assertNull(itemMapper.toItemWithBookingsAndCommentsDto(null, null, null, null));
    }
}
