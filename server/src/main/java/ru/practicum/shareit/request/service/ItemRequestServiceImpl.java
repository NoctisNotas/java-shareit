package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);
        ItemRequest savedRequest = requestRepository.save(request);

        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(request -> {
                    List<Item> items = itemRepository.findByRequestId(request.getId());
                    return ItemRequestMapper.toItemRequestResponseDto(request, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = requestRepository.findByRequestorIdNotOrderByCreatedDesc(userId, pageable);

        return requests.stream()
                .map(request -> {
                    List<Item> items = itemRepository.findByRequestId(request.getId());
                    return ItemRequestMapper.toItemRequestResponseDto(request, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long requestId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<Item> items = itemRepository.findByRequestId(requestId);
        return ItemRequestMapper.toItemRequestResponseDto(request, items);
    }
}