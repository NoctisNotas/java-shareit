package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryItemStorage implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public Item create(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ru.practicum.shareit.exception.ValidationException("Название вещи не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new ru.practicum.shareit.exception.ValidationException("Поле available не может быть пустым");
        }

        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException("Вещь с id " + item.getId() + " не найдена");
        }

        Item existingItem = items.get(item.getId());

        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }
        if (item.getRequestId() != null) {
            existingItem.setRequestId(item.getRequestId());
        }

        items.put(existingItem.getId(), existingItem);
        return existingItem;
    }

    @Override
    public Item getById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }
        return item;
    }

    @Override
    public List<Item> getByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && ownerId.equals(item.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(lowerText)) ||
                        (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerText)))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }
        items.remove(itemId);
    }
}