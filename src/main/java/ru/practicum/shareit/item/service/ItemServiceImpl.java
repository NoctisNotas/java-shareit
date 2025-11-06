package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        // Получаем UserDto из сервиса и преобразуем в User модель
        User owner = UserMapper.toUser(userService.getById(ownerId));
        Item item = ItemMapper.toItem(itemDto, owner);
        Item createdItem = itemRepository.create(item);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long ownerId) {
        Item existingItem = getItemById(itemDto.getId());

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        // Обновляем только переданные поля (PATCH логика)
        if (itemDto.getName() != null) existingItem.setName(itemDto.getName());
        if (itemDto.getDescription() != null) existingItem.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) existingItem.setAvailable(itemDto.getAvailable());
        if (itemDto.getRequestId() != null) existingItem.setRequestId(itemDto.getRequestId());

        Item updatedItem = itemRepository.update(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = getItemById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwnerId(Long ownerId) {
        // Проверяем, что пользователь существует
        userService.getById(ownerId);
        return itemRepository.getByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item item = getItemById(itemId);

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        itemRepository.delete(itemId);
    }

    private Item getItemById(Long itemId) {
        Item item = itemRepository.getById(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с id " + itemId + " не найдена");
        }
        return item;
    }
}