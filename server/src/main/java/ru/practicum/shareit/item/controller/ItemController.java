package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestBody ItemDto itemDto,
                          @RequestHeader(USER_ID_HEADER) Long ownerId) {
        return itemService.create(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @PathVariable Long itemId,
                          @RequestHeader(USER_ID_HEADER) Long ownerId) {
        itemDto.setId(itemId);
        return itemService.update(itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getById(@PathVariable Long itemId,
                                       @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingsDto> getByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        return itemService.getByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId,
                       @RequestHeader(USER_ID_HEADER) Long ownerId) {
        itemService.delete(itemId, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@PathVariable Long itemId,
                                         @RequestBody CommentDto commentDto,
                                         @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.addComment(itemId, commentDto, userId);
    }
}