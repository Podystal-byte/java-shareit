package ru.practicum.shareit;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsAndCommentsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("owner");

        booker = new User();
        booker.setId(2L);
        booker.setName("booker");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);

        item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setName("ItemDto");
        itemDto.setDescription("Description Dto");
        itemDto.setAvailable(true);
    }

    @Test
    void addItem_whenUserFoundAndRequestExists_shouldSaveItem() {
        itemDto.setRequestId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item savedItem = itemService.addItem(1L, itemDto);

        assertNotNull(savedItem);
        assertEquals(item.getName(), savedItem.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void addItem_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> itemService.addItem(99L, itemDto));
    }

    @Test
    void addItem_whenRequestNotFound_shouldThrowNotFoundException() {
        itemDto.setRequestId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(1L, itemDto));
    }

    @Test
    void updateItem_whenUserIsOwner_shouldUpdate() {
        Item updates = new Item();
        updates.setId(1L);
        updates.setName("Updated Name");
        updates.setAvailable(false);
        updates.setDescription("Updated Description");


        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updates);

        Item updatedItem = itemService.updateItem(1L, 1L, updates);

        assertEquals("Updated Name", updatedItem.getName());
        assertEquals(false, updatedItem.getAvailable());
        assertEquals("Updated Description", updatedItem.getDescription());

    }

    @Test
    void updateItem_whenUpdateContainsNulls_shouldNotUpdateFields() {
        Item updates = new Item();
        updates.setId(1L);
        updates.setName(null);
        updates.setAvailable(null);
        updates.setDescription(null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item updatedItem = itemService.updateItem(1L, 1L, updates);

        assertEquals(item.getName(), updatedItem.getName());
        assertEquals(item.getAvailable(), updatedItem.getAvailable());
        assertEquals(item.getDescription(), updatedItem.getDescription());
    }

    @Test
    void updateItem_whenUserIsNotOwner_shouldThrowNotFoundException() {
        Item itemToUpdate = new Item();
        itemToUpdate.setId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(NotFoundException.class, () -> itemService.updateItem(2L, 1L, itemToUpdate));
    }

    @Test
    void getItemById_whenUserIsOwner_shouldReturnDtoWithBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(1L)).thenReturn(Collections.emptyList());

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setBooker(booker);

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setBooker(booker);

        when(bookingRepository.findByItem_IdAndStartIsBeforeAndStatus(anyLong(), any(), eq(Status.APPROVED), any(Sort.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findByItem_IdAndStartIsAfterAndStatus(anyLong(), any(), eq(Status.APPROVED), any(Sort.class)))
                .thenReturn(List.of(nextBooking));

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertEquals(1L, result.getLastBooking().getId());
        assertEquals(2L, result.getNextBooking().getId());
    }

    @Test
    void getItemById_whenUserIsNotOwner_shouldReturnDtoWithoutBookings() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(1L)).thenReturn(Collections.emptyList());

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(1L, 2L);

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getItemsByOwner_shouldReturnListOfItems() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        List<Item> result = itemService.getItemsByOwner(1L);
        assertEquals(1, result.size());
    }

    @Test
    void searchItems_withValidText_shouldReturnItems() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        List<Item> result = itemService.searchItems("item");
        assertEquals(1, result.size());
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() {
        List<Item> result = itemService.searchItems(" ");
        assertTrue(result.isEmpty());
    }

    @Test
    void addComment_whenUserHasBooked_shouldSaveComment() {
        Comment comment = new Comment();
        comment.setText("Test");
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any()))
                .thenReturn(List.of(new Booking()));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment saved = itemService.addComment(2L, 1L, comment);

        assertNotNull(saved);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_whenUserHasNotBooked_shouldThrowValidationException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.addComment(2L, 1L, new Comment()));
    }

    @Test
    void findItemsByRequestId_shouldReturnItems() {
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));
        List<Item> items = itemService.findItemsByRequestId(1L);
        assertFalse(items.isEmpty());
    }
}

