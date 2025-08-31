package ru.practicum.shareit;


import jakarta.validation.ValidationException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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

    @Test
    public void addItem_Success_WithRequest() {
        User owner = new User();
        owner.setId(1L);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);

        ItemDto itemDto = new ItemDto();
        itemDto.setRequestId(1L);
        itemDto.setName("test item");
        itemDto.setDescription("test description");
        itemDto.setAvailable(true);

        Item item = new Item();
        item.setName("test item");
        item.setDescription("test description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(itemRequest);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item addedItem = itemService.addItem(owner.getId(), itemDto);

        assertNotNull(addedItem);
        assertEquals(item.getName(), addedItem.getName());
        assertEquals(item.getDescription(), addedItem.getDescription());
        assertEquals(owner.getId(), addedItem.getOwner().getId());
        assertEquals(itemRequest.getId(), addedItem.getRequest().getId());
    }

    @Test
    public void addItem_Success_WithoutRequest() {
        User owner = new User();
        owner.setId(1L);

        ItemDto itemDto = new ItemDto();
        itemDto.setRequestId(null);
        itemDto.setName("test item");
        itemDto.setDescription("test description");
        itemDto.setAvailable(true);

        Item item = new Item();
        item.setName("test item");
        item.setDescription("test description");
        item.setAvailable(true);
        item.setOwner(owner);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item addedItem = itemService.addItem(owner.getId(), itemDto);

        assertNotNull(addedItem);
        assertNull(addedItem.getRequest());
    }

    @Test
    public void addItem_UserNotFound_ThrowsException() {
        ItemDto itemDto = new ItemDto();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(1L, itemDto));
    }

    @Test
    public void addItem_RequestNotFound_ThrowsException() {
        User owner = new User();
        owner.setId(1L);

        ItemDto itemDto = new ItemDto();
        itemDto.setRequestId(1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(owner.getId(), itemDto));
    }

    @Test
    public void updateItem_Success() {
        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("old item");
        existingItem.setDescription("old description");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("updated item");
        updatedItem.setDescription("updated description");
        updatedItem.setAvailable(false);
        updatedItem.setOwner(owner);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        Item result = itemService.updateItem(owner.getId(), existingItem.getId(), updatedItem);

        assertNotNull(result);
        assertEquals(updatedItem.getName(), result.getName());
        assertEquals(updatedItem.getDescription(), result.getDescription());
        assertEquals(updatedItem.getAvailable(), result.getAvailable());
    }

    @Test
    public void updateItem_NotFound_ThrowsException() {
        Item updatedItem = new Item();
        updatedItem.setId(1L);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, 1L, updatedItem));
    }

    @Test
    public void updateItem_UserNotOwner_ThrowsException() {
        User owner = new User();
        owner.setId(1L);

        User notOwner = new User();
        notOwner.setId(2L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setOwner(owner);

        Item updatedItem = new Item();
        updatedItem.setId(1L);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(existingItem));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(notOwner.getId(), existingItem.getId(), updatedItem));
    }

    @Test
    public void updateItem_PartialUpdate_Success() {
        User owner = new User();
        owner.setId(1L);

        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("old name");
        existingItem.setDescription("old description");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("updated name");

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        Item result = itemService.updateItem(owner.getId(), existingItem.getId(), updatedItem);

        assertNotNull(result);
        assertEquals("updated name", result.getName());
        assertEquals("old description", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    public void getItemById_Success_WithBookingsAndComments() {
        User owner = new User();
        owner.setId(1L);

        User booker = new User();
        booker.setId(2L);

        Item item = new Item();
        item.setId(1L);
        item.setName("test item");
        item.setDescription("test description");
        item.setAvailable(true);
        item.setOwner(owner);

        Comment comment = new Comment();
        comment.setId(1L);

        LocalDateTime now = LocalDateTime.now();

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setBooker(booker);

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setBooker(booker);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(List.of(comment));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(null);
        when(bookingRepository.findByItem_IdAndStartIsBeforeAndStatus(anyLong(), any(LocalDateTime.class), any(Status.class), any(Sort.class))).thenReturn(List.of(lastBooking));
        when(bookingRepository.findByItem_IdAndStartIsAfterAndStatus(anyLong(), any(LocalDateTime.class), any(Status.class), any(Sort.class))).thenReturn(List.of(nextBooking));

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        assertEquals(lastBooking.getId(), result.getLastBooking().getId());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());
    }

    @Test
    public void getItemById_Success_WithoutBookings() {
        User owner = new User();
        owner.setId(1L);

        User booker = new User();
        booker.setId(2L);

        Item item = new Item();
        item.setId(1L);
        item.setName("test item");
        item.setDescription("test description");
        item.setAvailable(true);
        item.setOwner(owner);

        Comment comment = new Comment();
        comment.setId(1L);

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(commentRepository.findByItem_Id(anyLong())).thenReturn(List.of(comment));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(null);

        ItemWithBookingsAndCommentsDto result = itemService.getItemById(item.getId(), booker.getId());

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    public void getItemById_NotFound_ThrowsException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 1L));
    }

    @Test
    public void getItemsByOwner_Success() {
        User owner = new User();
        owner.setId(1L);

        Item item1 = new Item();
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setOwner(owner);

        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));

        List<Item> items = itemService.getItemsByOwner(owner.getId());

        assertNotNull(items);
        assertEquals(2, items.size());
    }

    @Test
    public void searchItems_Success() {
        Item item = new Item();
        item.setName("test item");
        item.setDescription("test description");
        item.setAvailable(true);

        when(itemRepository.findAll()).thenReturn(List.of(item));

        List<Item> foundItems = itemService.searchItems("test");

        assertNotNull(foundItems);
        assertEquals(1, foundItems.size());
        assertEquals("test item", foundItems.get(0).getName());
    }

    @Test
    public void searchItems_BlankText_ReturnsEmptyList() {
        List<Item> foundItems = itemService.searchItems("");

        assertNotNull(foundItems);
        assertTrue(foundItems.isEmpty());
    }

    @Test
    public void searchItems_ItemNotAvailable_ReturnsEmptyList() {
        Item item = new Item();
        item.setName("test item");
        item.setDescription("test description");
        item.setAvailable(false);

        when(itemRepository.findAll()).thenReturn(List.of(item));

        List<Item> foundItems = itemService.searchItems("test");

        assertNotNull(foundItems);
        assertTrue(foundItems.isEmpty());
    }

    @Test
    public void addComment_Success() {
        User booker = new User();
        booker.setId(1L);

        Item item = new Item();
        item.setId(1L);

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);

        Comment comment = new Comment();
        comment.setText("test comment");
        comment.setAuthor(booker);
        comment.setItem(item);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(List.of(lastBooking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment addedComment = itemService.addComment(booker.getId(), item.getId(), comment);

        assertNotNull(addedComment);
        assertEquals(comment.getText(), addedComment.getText());
        assertEquals(booker.getId(), addedComment.getAuthor().getId());
        assertEquals(item.getId(), addedComment.getItem().getId());
    }

    @Test
    public void addComment_UserNotBookedItem_ThrowsException() {
        User booker = new User();
        booker.setId(1L);

        Item item = new Item();
        item.setId(1L);

        Comment comment = new Comment();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.addComment(booker.getId(), item.getId(), comment));
    }

    @Test
    public void addComment_UserNotFound_ThrowsException() {
        Comment comment = new Comment();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(1L, 1L, comment));
    }

    @Test
    public void addComment_ItemNotFound_ThrowsException() {
        User booker = new User();
        booker.setId(1L);

        Comment comment = new Comment();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(booker.getId(), 1L, comment));
    }

    @Test
    public void findItemsByRequestId_Success() {
        Item item = new Item();
        item.setId(1L);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        item.setRequest(itemRequest);

        when(itemRepository.findByRequestId(anyLong())).thenReturn(List.of(item));

        List<Item> foundItems = itemService.findItemsByRequestId(itemRequest.getId());

        assertNotNull(foundItems);
        assertEquals(1, foundItems.size());
        assertEquals(item.getId(), foundItems.get(0).getId());
    }
}
