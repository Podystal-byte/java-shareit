package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.CommentService;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final CommentService commentService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader("X-Sharer-User-Id") Long userId, @Valid @RequestBody ItemDto itemDto) {
        Item item = itemMapper.toItem(itemDto);
        Item createdItem = itemService.addItem(userId, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemMapper.toDto(createdItem));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody ItemDto itemDto) {
        Item itemToUpdate = itemMapper.toItem(itemDto);
        itemToUpdate.setId(itemId);
        Item updatedItem = itemService.updateItem(userId, itemId, itemToUpdate);
        return ResponseEntity.ok(itemMapper.toDto(updatedItem));
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsAndCommentsDto getItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getOwnersItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = itemService.getItemsByOwner(userId).stream().map(itemMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        List<ItemDto> foundItems = itemService.searchItems(text).stream().map(itemMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(foundItems);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentRequestDto commentRequestDto) throws ValidationException {

        return commentMapper.toCommentDto(commentService.addComment(userId, itemId, commentRequestDto));
    }
}