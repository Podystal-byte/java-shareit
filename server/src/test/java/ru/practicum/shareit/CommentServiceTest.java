package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Item item;
    private CommentRequestDto commentRequestDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        item = new Item();
        item.setId(1L);

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Test comment text");

        booking = new Booking();
        booking.setBooker(user);
        booking.setItem(item);
        booking.setEnd(LocalDateTime.now().minusDays(1));
    }

    @Test
    void addComment_whenUserAndItemFoundAndBookingExists_thenSavesComment() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());

        assertDoesNotThrow(() -> commentService.addComment(1L, 1L, commentRequestDto));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void addComment_whenUserNotFound_thenThrowsNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.addComment(99L, 1L, commentRequestDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_whenItemNotFound_thenThrowsNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.addComment(1L, 99L, commentRequestDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_whenNoBookingFound_thenThrowsValidationException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> commentService.addComment(1L, 1L, commentRequestDto));
        verify(commentRepository, never()).save(any(Comment.class));
    }
}