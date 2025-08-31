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
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

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
    private User otherUser;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("John Doe");
        owner.setEmail("john.doe@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Jane Smith");
        otherUser.setEmail("jane.smith@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Drill");
        itemDto.setDescription("Powerful drill");
        itemDto.setAvailable(true);
    }

    @Test
    public void addItem_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item savedItem = itemService.addItem(owner.getId(), itemDto);

        assertNotNull(savedItem);
        assertEquals(item.getName(), savedItem.getName());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void addItem_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(owner.getId(), itemDto));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void addItem_WithRequest_Success() {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemDto.setRequestId(itemRequest.getId());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item savedItem = itemService.addItem(owner.getId(), itemDto);

        assertNotNull(savedItem);
        verify(itemRequestRepository, times(1)).findById(itemDto.getRequestId());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void updateItem_Success() {

        Item updatedFields = new Item();
        updatedFields.setId(item.getId());
        updatedFields.setName("Updated Drill");
        updatedFields.setDescription("Updated description");

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item result = itemService.updateItem(owner.getId(), item.getId(), updatedFields);

        assertNotNull(result);
        assertEquals("Updated Drill", result.getName());
        assertEquals("Updated description", result.getDescription());
        verify(itemRepository, times(1)).findById(item.getId());
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    public void updateItem_NotOwner_ThrowsException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        Item updatedFields = new Item();
        updatedFields.setId(item.getId());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(otherUser.getId(), item.getId(), updatedFields));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    public void getItemById_Owner_HasBookings() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(new ArrayList<>());

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setBooker(otherUser);

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setBooker(otherUser);

        when(bookingRepository.findByItem_IdAndStartIsBeforeAndStatus(anyLong(), any(LocalDateTime.class), eq(Status.APPROVED), any(Sort.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findByItem_IdAndStartIsAfterAndStatus(anyLong(), any(LocalDateTime.class), eq(Status.APPROVED), any(Sort.class)))
                .thenReturn(List.of(nextBooking));

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertEquals(lastBooking.getId(), result.getLastBooking().getId());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());
    }

    @Test
    public void getItemById_NotOwner_NoBookingsReturned() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(new ArrayList<>());

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(item.getId(), otherUser.getId());

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    public void searchItems_BlankText_ReturnsEmptyList() {
        String searchText = " ";

        List<Item> result = itemService.searchItems(searchText);

        assertTrue(result.isEmpty());
    }

    @Test
    public void searchItems_WithText_ReturnsMatchingItems() {
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Scooter");
        item2.setDescription("Electric scooter");
        item2.setAvailable(true);
        item2.setOwner(owner);

        Item item3 = new Item();
        item3.setId(3L);
        item3.setName("Car");
        item3.setDescription("Old car");
        item3.setAvailable(false);
        item3.setOwner(owner);

        when(itemRepository.findAll()).thenReturn(List.of(item, item2, item3));

        List<Item> result = itemService.searchItems("drill");

        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    public void addComment_Success() {
        Comment comment = new Comment();
        comment.setText("Great item!");

        Booking booking = new Booking();
        booking.setBooker(otherUser);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = itemService.addComment(otherUser.getId(), item.getId(), comment);

        assertNotNull(result);
        assertEquals("Great item!", result.getText());
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    public void addComment_NoPastBookings_ThrowsException() {
        Comment comment = new Comment();
        comment.setText("Great item!");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(otherUser));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        assertThrows(jakarta.validation.ValidationException.class, () -> itemService.addComment(otherUser.getId(), item.getId(), comment));
        verify(commentRepository, never()).save(any(Comment.class));
    }
}
