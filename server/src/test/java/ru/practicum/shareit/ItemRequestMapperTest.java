package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ItemRequestMapperImpl.class, ItemMapperImpl.class})
class ItemRequestMapperTest {

    @Autowired
    private ItemRequestMapper itemRequestMapper;

    @Test
    void toItem() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a hammer");
        dto.setCreated(LocalDateTime.now());
        dto.setItems(Collections.emptyList());

        // When
        ItemRequest result = itemRequestMapper.toItem(dto);

        // Then
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getCreated(), result.getCreated());
    }

    @Test
    void toItem_whenDtoIsNull_shouldReturnNull() {
        // When
        ItemRequest result = itemRequestMapper.toItem(null);

        // Then
        assertNull(result);
    }

    @Test
    void toDto() {
        // Given
        User requester = new User();
        requester.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Need a screwdriver");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setItems(Collections.emptyList());

        // When
        ItemRequestDto result = itemRequestMapper.toDto(itemRequest);

        // Then
        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void toDto_whenRequestIsNull_shouldReturnNull() {
        // When
        ItemRequestDto result = itemRequestMapper.toDto(null);

        // Then
        assertNull(result);
    }
}
