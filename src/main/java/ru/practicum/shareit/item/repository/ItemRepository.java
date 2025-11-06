package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item create(Item item);

    Item update(Item item);

    Item getById(Long itemId);

    List<Item> getByOwnerId(Long ownerId);

    List<Item> search(String text);

    void delete(Long itemId);
}