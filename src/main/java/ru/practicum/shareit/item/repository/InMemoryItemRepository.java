package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Integer, Item> items = new HashMap<>();
    private int nextId = 1;

    @Override
    public Item create(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (items.containsKey(item.getId())) {
            items.put(item.getId(), item);
            return item;
        }
        return null;
    }

    @Override
    public void delete(int itemId) {
        items.remove(itemId);
    }

    @Override
    public Item findById(int itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }
}