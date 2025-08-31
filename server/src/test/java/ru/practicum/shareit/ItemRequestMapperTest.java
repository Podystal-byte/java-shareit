package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a hammer");
        dto.setCreated(LocalDateTime.now());
        dto.setItems(Collections.emptyList());

        ItemRequest result = itemRequestMapper.toItem(dto);

        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(dto.getCreated(), result.getCreated());
    }

    @Test
    void toItem_whenDtoIsNull_shouldReturnNull() {
        ItemRequest result = itemRequestMapper.toItem(null);

        assertNull(result);
    }

    @Test
    void toDto() {
        User requester = new User();
        requester.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Need a screwdriver");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setItems(Collections.emptyList());

        ItemRequestDto result = itemRequestMapper.toDto(itemRequest);


        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void toDto_whenRequestIsNull_shouldReturnNull() {
        ItemRequestDto result = itemRequestMapper.toDto(null);

        assertNull(result);
    }
}
