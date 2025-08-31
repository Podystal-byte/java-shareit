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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = {ItemRequestMapperImpl.class, ItemMapperImpl.class})
public class ItemRequestMapperTest {

    @Autowired
    private ItemRequestMapper itemRequestMapper;

    @Test
    public void toItem_whenItemRequestDtoIsValid_thenCorrectlyMapped() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Test description");
        itemRequestDto.setCreated(LocalDateTime.now());
        itemRequestDto.setItems(null);

        ItemRequest itemRequest = itemRequestMapper.toItem(itemRequestDto);

        assertEquals(itemRequestDto.getId(), itemRequest.getId());
        assertEquals(itemRequestDto.getDescription(), itemRequest.getDescription());
        assertEquals(itemRequestDto.getCreated(), itemRequest.getCreated());
        assertNull(itemRequest.getItems());
    }

    @Test
    public void toItem_whenItemRequestDtoIsNull_thenReturnsNull() {
        ItemRequestDto itemRequestDto = null;

        ItemRequest itemRequest = itemRequestMapper.toItem(itemRequestDto);

        assertNull(itemRequest);
    }

    @Test
    public void toDto_whenItemRequestIsValid_thenCorrectlyMapped() {
        User requester = new User();
        requester.setId(1L);
        requester.setName("User Name");
        requester.setEmail("user@test.ru");

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Test description");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setItems(new ArrayList<>());

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertEquals(itemRequest.getId(), itemRequestDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestDto.getDescription());
        assertEquals(itemRequest.getCreated(), itemRequestDto.getCreated());
        assertEquals(0, itemRequestDto.getItems().size());
    }

    @Test
    public void toDto_whenItemRequestIsNull_thenReturnsNull() {
        ItemRequest itemRequest = null;

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertNull(itemRequestDto);
    }
}
