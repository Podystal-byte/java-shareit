package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public Item addItem(int userId, Item item) {
        User owner = userRepository.findById(userId);
        if (owner == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }

        item.setOwner(owner);
        log.info("Добавляем новую вещь: {}", item.getName());
        return itemRepository.create(item);
    }

    public Item updateItem(int userId, int itemId, Item updatedItem) {
        Item existingItem = itemRepository.findById(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена.");
        }

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
        return itemRepository.update(existingItem);
    }

    public Item getItemById(int itemId) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            log.warn("Вещь с id {} не найдена.", itemId);
            throw new NotFoundException("Вещь не найдена");
        }
        return item;
    }

    public List<Item> getItemsByOwner(int userId) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getOwner() != null && item.getOwner().getId() == userId)
                .collect(Collectors.toList());
    }

    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String searchText = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()) &&
                        (item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
    }
}