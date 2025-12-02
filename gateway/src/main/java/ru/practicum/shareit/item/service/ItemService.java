package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;
import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(ItemDto itemDto, Long ownerId);

    ItemWithBookingsDto getById(Long itemId, Long userId);

    List<ItemWithBookingsDto> getByOwnerId(Long ownerId);

    List<ItemDto> search(String text);

    void delete(Long itemId, Long ownerId);

    CommentResponseDto addComment(Long itemId, CommentDto commentDto, Long userId);
}