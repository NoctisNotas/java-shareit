package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean isAvailable;
    private User owner;
    private Long itemRequestId;
}
