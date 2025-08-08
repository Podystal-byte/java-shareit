package ru.practicum.shareit.item.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public Item addItem(int userId, Item item) {
        User owner = userRepository.findById(userId);
        if (owner == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }

        item.setOwner(owner);
        log.info("Добавлена новая вещь: {}", item.getName());
        return itemRepository.create(item);
    }

    public Item updateItem(int userId, int itemId, Item updatedItem) {
        Item existingItem = itemRepository.findById(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена.");
        }

        if (existingItem.getOwner().getId() != userId) {
            log.warn("Попытка редактирования вещи {} не владельцем {}", itemId, userId);
            throw new NotFoundException("Редактировать вещь может только её владелец.");
        }

        if (updatedItem.getName() != null) {
            existingItem.setName(updatedItem.getName());
        }
        if (updatedItem.getDescription() != null) {
            existingItem.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.isAvailable() != existingItem.isAvailable()) {
            existingItem.setAvailable(updatedItem.isAvailable());
        }

        log.info("Вещь с id {} обновлена", itemId);
        return itemRepository.update(existingItem);
    }

    public Item getItemById(int itemId) {
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            log.warn("Вещь с id {} не найдена", itemId);
            throw new NotFoundException("Item");
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
                .filter(item -> item.isAvailable() &&
                        (item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
    }
}