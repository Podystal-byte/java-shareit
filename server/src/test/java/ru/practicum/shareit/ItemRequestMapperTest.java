package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ItemRequestMapperTest {

    private ItemRequestMapper itemRequestMapper;

    @BeforeEach
    public void setUp() {
        itemRequestMapper = Mappers.getMapper(ItemRequestMapper.class);
    }

    @Test
    public void toItem_Success() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Test Description");

        ItemRequest itemRequest = itemRequestMapper.toItem(itemRequestDto);

        assertNotNull(itemRequest);
        assertEquals(itemRequestDto.getDescription(), itemRequest.getDescription());
    }

    @Test
    public void toDto_Success() {
        User requester = new User();
        requester.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test Description");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.of(2024, 8, 31, 12, 0));

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(request);

        assertNotNull(itemRequestDto);
        assertEquals(request.getId(), itemRequestDto.getId());
        assertEquals(request.getDescription(), itemRequestDto.getDescription());
        assertEquals(request.getCreated(), itemRequestDto.getCreated());
    }

    @Test
    public void toDto_WhenItemRequestHasNoItems_ReturnsEmptyList() {
        User requester = new User();
        requester.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Test Description");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.of(2024, 8, 31, 12, 0));

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(request);

        assertNotNull(itemRequestDto);
        assertNotNull(itemRequestDto.getItems());
        assertTrue(itemRequestDto.getItems().isEmpty());
    }
}
