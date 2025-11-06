package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.create(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@Valid @RequestBody ItemDto itemDto,
                          @PathVariable Long itemId,
                          @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        itemDto.setId(itemId);
        return itemService.update(itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId,
                       @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        itemService.delete(itemId, ownerId);
    }
}
