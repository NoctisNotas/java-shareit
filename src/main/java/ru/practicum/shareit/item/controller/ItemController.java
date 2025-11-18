package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader(USER_ID_HEADER) Long ownerId) {
        return itemService.create(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@Valid @RequestBody ItemUpdateDto itemUpdateDto,
                          @PathVariable Long itemId,
                          @RequestHeader(USER_ID_HEADER) Long ownerId) {
        ItemDto itemDto = new ItemDto(itemId, itemUpdateDto.getName(),
                itemUpdateDto.getDescription(),
                itemUpdateDto.getAvailable(),
                itemUpdateDto.getRequestId());
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
                                         @Valid @RequestBody CommentDto commentDto,
                                         @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.addComment(itemId, commentDto, userId);
    }
}
