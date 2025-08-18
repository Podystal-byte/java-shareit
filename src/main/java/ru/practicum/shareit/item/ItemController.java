package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader("X-Sharer-User-Id") int userId,
                                           @Valid @RequestBody ItemDto itemDto) {
        Item item = itemMapper.toItem(itemDto);
        Item createdItem = itemService.addItem(userId, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemMapper.toDto(createdItem));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") int userId,
                                              @PathVariable int itemId,
                                              @RequestBody ItemDto itemDto) {
        Item itemToUpdate = itemMapper.toItem(itemDto);
        Item updatedItem = itemService.updateItem(userId, itemId, itemToUpdate);
        return ResponseEntity.ok(itemMapper.toDto(updatedItem));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable int itemId) {
        Item item = itemService.getItemById(itemId);
        return ResponseEntity.ok(itemMapper.toDto(item));
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getOwnersItems(@RequestHeader("X-Sharer-User-Id") int userId) {
        List<ItemDto> items = itemService.getItemsByOwner(userId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        List<ItemDto> foundItems = itemService.searchItems(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(foundItems);
    }
}