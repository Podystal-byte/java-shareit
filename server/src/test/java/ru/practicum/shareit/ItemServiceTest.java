package ru.practicum.shareit;

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
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
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
    private CommentMapper commentMapper;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("owner");
        owner.setEmail("owner@user.ru");

        booker = new User();
        booker.setId(2L);
        booker.setName("booker");
        booker.setEmail("booker@user.ru");

        item = new Item();
        item.setId(1L);
        item.setName("testItem");
        item.setDescription("testDescription");
        item.setAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setName("testItem");
        itemDto.setDescription("testDescription");
        itemDto.setAvailable(true);
    }

    @Test
    void addItem_whenUserFound_thenSavesItem() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item savedItem = itemService.addItem(1L, itemDto);

        assertNotNull(savedItem);
        assertEquals(item.getName(), savedItem.getName());
        verify(userRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void addItem_whenUserNotFound_thenThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(99L, itemDto));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_whenUserIsOwner_thenUpdatesItem() {
        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("updatedName");
        updatedItem.setDescription("updatedDescription");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item result = itemService.updateItem(1L, 1L, updatedItem);

        assertNotNull(result);
        assertEquals("updatedName", result.getName());
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_whenUserIsNotOwner_thenThrowsException() {
        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("updatedName");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(99L, 1L, updatedItem));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void getItemById_whenUserIsOwner_thenReturnsItemWithBookings() {
        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setBooker(booker);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(Collections.emptyList());
        when(bookingRepository.findByItem_IdAndStartIsBeforeAndStatus(anyLong(), any(LocalDateTime.class), any(Status.class), any(Sort.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findByItem_IdAndStartIsAfterAndStatus(anyLong(), any(LocalDateTime.class), any(Status.class), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertEquals(2L, result.getLastBooking().getBookerId());
        assertNull(result.getNextBooking());
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_whenUserIsNotOwner_thenReturnsItemWithoutBookings() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(Collections.emptyList());

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(1L, 99L);

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).findByItem_IdAndStartIsAfterAndStatus(anyLong(), any(LocalDateTime.class), any(Status.class), any(Sort.class));
    }

    @Test
    void getItemsByOwner_whenCalled_thenReturnsOwnersItems() {
        when(itemRepository.findAll()).thenReturn(List.of(item));
        List<Item> items = itemService.getItemsByOwner(1L);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getOwner().getId());
    }

    @Test
    void searchItems_whenTextIsValid_thenReturnsMatchingItems() {
        Item foundItem = new Item();
        foundItem.setName("searchable item");
        foundItem.setAvailable(true);
        foundItem.setDescription("a test item");

        when(itemRepository.findAll()).thenReturn(List.of(foundItem));

        List<Item> items = itemService.searchItems("searchable");
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
    }

    @Test
    void searchItems_whenTextIsBlank_thenReturnsEmptyList() {
        List<Item> items = itemService.searchItems("  ");
        assertNotNull(items);
        assertTrue(items.isEmpty());
        verify(itemRepository, never()).findAll();
    }

    @Test
    void addComment_whenUserBookedItem_thenSavesComment() {
        Comment comment = new Comment();
        comment.setText("test comment");

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setEnd(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment savedComment = itemService.addComment(2L, 1L, comment);

        assertNotNull(savedComment);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_whenUserDidNotBookItem_thenThrowsValidationException() {
        Comment comment = new Comment();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.addComment(2L, 1L, comment));
        verify(commentRepository, never()).save(any(Comment.class));
    }
}