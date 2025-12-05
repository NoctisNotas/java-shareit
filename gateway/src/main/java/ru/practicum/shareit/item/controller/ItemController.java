package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemDto itemDto,
                                         @RequestHeader(USER_ID_HEADER) Long ownerId) {
        return itemClient.create(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@Valid @RequestBody ItemUpdateDto itemUpdateDto,
                                         @PathVariable Long itemId,
                                         @RequestHeader(USER_ID_HEADER) Long ownerId) {
        ItemDto itemDto = new ItemDto(itemId, itemUpdateDto.getName(),
                itemUpdateDto.getDescription(),
                itemUpdateDto.getAvailable(),
                itemUpdateDto.getRequestId());
        return itemClient.update(itemId, itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable Long itemId,
                                          @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemClient.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        return itemClient.getByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        return itemClient.search(text);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@PathVariable Long itemId,
                                         @RequestHeader(USER_ID_HEADER) Long ownerId) {
        return itemClient.delete(itemId, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto,
                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemClient.addComment(itemId, commentDto, userId);
    }
}