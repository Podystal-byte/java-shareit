package ru.practicum.shareit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;

class ItemRequestMapperImplTest {

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestMapperImpl itemRequestMapper;

    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        item = new Item();
        item.setId(1L);
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setAvailable(true);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("ItemDto Name");
        itemDto.setDescription("ItemDto Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(1L);

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Request Description");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setItems(List.of(item));

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Request Dto Description");
        itemRequestDto.setCreated(LocalDateTime.now());
        itemRequestDto.setItems(List.of(itemDto));
    }

    @Test
    void toItem_shouldMapItemRequestDtoToItemRequest() {
        when(itemMapper.fromDto(itemRequestDto.getItems().get(0))).thenReturn(item);

        ItemRequest mappedItemRequest = itemRequestMapper.toItem(itemRequestDto);

        assertNotNull(mappedItemRequest);
        assertEquals(itemRequestDto.getId(), mappedItemRequest.getId());
        assertEquals(itemRequestDto.getDescription(), mappedItemRequest.getDescription());
        assertEquals(itemRequestDto.getCreated(), mappedItemRequest.getCreated());
        assertEquals(1, mappedItemRequest.getItems().size());
        assertEquals(item.getId(), mappedItemRequest.getItems().get(0).getId());
    }

    @Test
    void toItem_shouldReturnNullForNullInput() {
        assertNull(itemRequestMapper.toItem(null));
    }

    @Test
    void toItem_shouldHandleNullItemsList() {
        itemRequestDto.setItems(null);
        ItemRequest mappedItemRequest = itemRequestMapper.toItem(itemRequestDto);
        assertNotNull(mappedItemRequest);
        assertNull(mappedItemRequest.getItems());
    }

    @Test
    void toItem_shouldHandleEmptyItemsList() {
        itemRequestDto.setItems(Collections.emptyList());
        ItemRequest mappedItemRequest = itemRequestMapper.toItem(itemRequestDto);
        assertNotNull(mappedItemRequest);
        assertEquals(0, mappedItemRequest.getItems().size());
    }

    @Test
    void toDto_shouldMapItemRequestToItemRequestDto() {
        when(itemMapper.toDto(itemRequest.getItems().get(0))).thenReturn(itemDto);

        ItemRequestDto mappedItemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertNotNull(mappedItemRequestDto);
        assertEquals(itemRequest.getId(), mappedItemRequestDto.getId());
        assertEquals(itemRequest.getDescription(), mappedItemRequestDto.getDescription());
        assertEquals(itemRequest.getCreated(), mappedItemRequestDto.getCreated());
        assertEquals(1, mappedItemRequestDto.getItems().size());
        assertEquals(itemDto.getId(), mappedItemRequestDto.getItems().get(0).getId());
    }

    @Test
    void toDto_shouldReturnNullForNullInput() {
        assertNull(itemRequestMapper.toDto(null));
    }

    @Test
    void toDto_shouldHandleNullItemsList() {
        itemRequest.setItems(null);
        ItemRequestDto mappedItemRequestDto = itemRequestMapper.toDto(itemRequest);
        assertNotNull(mappedItemRequestDto);
        assertNull(mappedItemRequestDto.getItems());
    }

    @Test
    void toDto_shouldHandleEmptyItemsList() {
        itemRequest.setItems(Collections.emptyList());
        ItemRequestDto mappedItemRequestDto = itemRequestMapper.toDto(itemRequest);
        assertNotNull(mappedItemRequestDto);
        assertEquals(0, mappedItemRequestDto.getItems().size());
    }
}
