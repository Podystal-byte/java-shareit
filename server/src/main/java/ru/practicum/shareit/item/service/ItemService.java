package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingItemDto;
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
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repo.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    public Item addItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest itemRequest = null;
        if (itemDto.getRequestId() != null) {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found"));
        }

        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequest(itemRequest); // ЭТОГО ДОСТАТОЧНО

        Item savedItem = itemRepository.save(item);

        log.info("Добавляем новую вещь: {}", savedItem.getName());
        return savedItem;
    }

    public Item updateItem(Long userId, Long itemId, Item updatedItem) {
        Item existingItem = itemRepository.findById(updatedItem.getId()).orElseThrow(() -> new NotFoundException("item not found"));

        if (!Objects.equals(existingItem.getOwner().getId(), userId)) {
            log.warn("Пользователь {} попытался отредактировать вещь {}, которой не владеет.", userId, itemId);
            throw new NotFoundException("Только владелец может редактировать вещь.");
        }

        if (updatedItem.getName() != null) {
            existingItem.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            existingItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getAvailable() != null) {
            existingItem.setAvailable(updatedItem.getAvailable());
        }

        log.info("Вещь с id {} была обновлена.", itemId);
        return itemRepository.save(existingItem);
    }

    public ItemWithBookingsAndCommentsDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("item not found"));

        List<Comment> comments = commentRepository.findByItem_Id(itemId);

        ItemWithBookingsAndCommentsDto itemDto = new ItemWithBookingsAndCommentsDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setOwnerId(item.getOwner().getId());

        itemDto.setComments(comments.stream()
                .map(commentMapper::toCommentDto)
                .toList());

        if (Objects.equals(item.getOwner().getId(), userId)) {
            LocalDateTime now = LocalDateTime.now();

            Optional<Booking> lastBooking = bookingRepository
                    .findByItem_IdAndStartIsBeforeAndStatus(itemId, now, Status.APPROVED, Sort.by(Sort.Direction.DESC, "start"))
                    .stream()
                    .findFirst();

            Optional<Booking> nextBooking = bookingRepository
                    .findByItem_IdAndStartIsAfterAndStatus(itemId, now, Status.APPROVED, Sort.by(Sort.Direction.ASC, "start"))
                    .stream()
                    .findFirst();

            lastBooking.ifPresent(booking -> {
                BookingItemDto bookingDto = new BookingItemDto();
                bookingDto.setId(booking.getId());
                bookingDto.setBookerId(booking.getBooker().getId());
                itemDto.setLastBooking(bookingDto);
            });

            nextBooking.ifPresent(booking -> {
                BookingItemDto bookingDto = new BookingItemDto();
                bookingDto.setId(booking.getId());
                bookingDto.setBookerId(booking.getBooker().getId());
                itemDto.setNextBooking(bookingDto);
            });
        }

        return itemDto;
    }

    public List<Item> getItemsByOwner(Long userId) {
        return itemRepository.findAll().stream().filter(item -> item.getOwner() != null && Objects.equals(item.getOwner().getId(), userId)).toList();
    }

    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String searchText = text.toLowerCase();
        return itemRepository.findAll().stream().filter(item -> Boolean.TRUE.equals(item.getAvailable()) && (item.getName().toLowerCase().contains(searchText) || item.getDescription().toLowerCase().contains(searchText))).toList();
    }

    public Comment addComment(Long userId, Long itemId, Comment comment) {
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена."));

        List<Booking> userBookings = bookingRepository.findByBookerIdAndItem_IdAndEndIsBefore(userId, itemId, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            throw new ValidationException("Пользователь не может оставлять комментарии, так как не брал вещь в аренду.");
        }

        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public List<Item> findItemsByRequestId(Long requestId) {
        return itemRepository.findByRequestId(requestId);
    }
}