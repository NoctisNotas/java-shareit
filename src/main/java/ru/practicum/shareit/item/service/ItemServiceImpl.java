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
        User owner = getUserById(ownerId);
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

        Item itemToUpdate = new Item();
        itemToUpdate.setId(itemDto.getId());
        itemToUpdate.setName(itemDto.getName());
        itemToUpdate.setDescription(itemDto.getDescription());
        itemToUpdate.setAvailable(itemDto.getAvailable());
        itemToUpdate.setRequestId(itemDto.getRequestId());

        Item updatedItem = itemRepository.update(itemToUpdate);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = getItemById(itemId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwnerId(Long ownerId) {
        getUserById(ownerId);
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
        return itemRepository.getById(itemId);
    }

    private User getUserById(Long userId) {
        ru.practicum.shareit.user.dto.UserDto userDto = userService.getById(userId);
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }
}