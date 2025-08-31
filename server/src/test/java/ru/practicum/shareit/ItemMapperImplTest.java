package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperImplTest {

    @InjectMocks
    private ItemMapperImpl itemMapper;

    private Item item;
    private ItemDto itemDto;
    private User owner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        owner = new User();
        owner.setId(1L);
        owner.setName("Test User");
        owner.setEmail("test@user.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test ItemDto");
        itemDto.setDescription("Test Dto Description");
        itemDto.setAvailable(true);
        itemDto.setOwnerId(1);
    }

    @Test
    void toDto_shouldMapItemToItemDto() {
        ItemDto mappedItemDto = itemMapper.toDto(item);

        assertNotNull(mappedItemDto);
        assertEquals(item.getId(), mappedItemDto.getId());
        assertEquals(item.getName(), mappedItemDto.getName());
        assertEquals(item.getDescription(), mappedItemDto.getDescription());
        assertEquals(item.getAvailable(), mappedItemDto.getAvailable());
    }

    @Test
    void toDto_shouldReturnNullForNullInput() {
        assertNull(itemMapper.toDto(null));
    }

    @Test
    void fromDto_shouldMapItemDtoToItem() {
        Item mappedItem = itemMapper.fromDto(itemDto);

        assertNotNull(mappedItem);
        assertEquals(itemDto.getId(), mappedItem.getId());
        assertEquals(itemDto.getName(), mappedItem.getName());
        assertEquals(itemDto.getDescription(), mappedItem.getDescription());
        assertEquals(itemDto.getAvailable(), mappedItem.getAvailable());
        assertNull(mappedItem.getOwner());
    }

    @Test
    void fromDto_shouldReturnNullForNullInput() {
        assertNull(itemMapper.fromDto(null));
    }

    @Test
    void toDto_shouldHandleNullOwner() {
        item.setOwner(null);

        ItemDto mappedItemDto = itemMapper.toDto(item);

        assertNotNull(mappedItemDto);
        assertNull(mappedItemDto.getOwnerId());
    }
}
