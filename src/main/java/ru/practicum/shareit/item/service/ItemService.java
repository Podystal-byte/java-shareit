package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repo.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    public Item addItem(Long userId, Item item) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));


        item.setOwner(owner);
        log.info("Добавляем новую вещь: {}", item.getName());
        return itemRepository.save(item);
    }

    public Item updateItem(Long userId, Long itemId, Item updatedItem) {
        Item existingItem = itemRepository.findById(updatedItem.getId()).orElseThrow(() -> new NotFoundException("item not found"));

        if (existingItem.getOwner().getId() != userId) {
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

    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("item not found"));
    }

    public List<Item> getItemsByOwner(Long userId) {
        return itemRepository.findAll().stream().filter(item -> item.getOwner() != null && item.getOwner().getId() == userId).toList();
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
}