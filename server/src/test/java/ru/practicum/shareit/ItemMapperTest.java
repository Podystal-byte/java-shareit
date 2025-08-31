package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = ItemMapperImpl.class)
public class ItemMapperTest {

    @Autowired
    private ItemMapper itemMapper;

    @Test
    public void itemDtoToItem_whenDtoIsValid_thenCorrectlyMapped() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        Item item = itemMapper.fromDto(itemDto);

        assertNotNull(item);
        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getAvailable(), item.getAvailable());
    }

    @Test
    public void itemDtoToItem_whenDtoIsNull_thenReturnsNull() {
        ItemDto itemDto = null;

        Item item = itemMapper.fromDto(itemDto);

        assertNull(item);
    }

    @Test
    public void itemToDto_whenItemIsValid_thenCorrectlyMapped() {

        User owner = new User();
        owner.setId(1L);
        owner.setName("Test User");
        owner.setEmail("test@user.ru");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemDto itemDto = itemMapper.toDto(item);

        assertNotNull(itemDto);
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
    }

    @Test
    public void itemToDto_whenItemIsNull_thenReturnsNull() {
        Item item = null;

        ItemDto itemDto = itemMapper.toDto(item);

        assertNull(itemDto);
    }
}
