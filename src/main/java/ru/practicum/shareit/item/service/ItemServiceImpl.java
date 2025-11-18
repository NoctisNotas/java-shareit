package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        userService.getById(ownerId);

        User owner = new User();
        owner.setId(ownerId);

        Item item = ItemMapper.toItem(itemDto, owner);
        Item createdItem = itemRepository.save(item);
        return ItemMapper.toItemDto(createdItem);
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemDto.getId() + " не найдена"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getRequestId() != null) {
            existingItem.setRequestId(itemDto.getRequestId());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        List<CommentResponseDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(CommentMapper::toCommentResponseDto)
                .collect(Collectors.toList());

        ItemWithBookingsDto itemWithBookingsDto = new ItemWithBookingsDto();
        itemWithBookingsDto.setId(item.getId());
        itemWithBookingsDto.setName(item.getName());
        itemWithBookingsDto.setDescription(item.getDescription());
        itemWithBookingsDto.setAvailable(item.getAvailable());
        itemWithBookingsDto.setRequestId(item.getRequestId());
        itemWithBookingsDto.setComments(comments);

        // Показываем информацию о бронированиях только владельцу
        if (item.getOwner().getId().equals(userId)) {
            // Последнее бронирование (закончившееся или текущее)
            List<Booking> lastBookings = bookingRepository.findByItemIdAndEndBeforeOrderByEndDesc(
                    itemId, LocalDateTime.now());
            if (!lastBookings.isEmpty()) {
                Booking lastBooking = lastBookings.get(0);
                itemWithBookingsDto.setLastBooking(new BookingInfoDto(
                        lastBooking.getId(),
                        lastBooking.getBooker().getId(),
                        lastBooking.getStart(),
                        lastBooking.getEnd()
                ));
            }

            // Следующее бронирование
            List<Booking> nextBookings = bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(
                    itemId, LocalDateTime.now());
            if (!nextBookings.isEmpty()) {
                Booking nextBooking = nextBookings.get(0);
                itemWithBookingsDto.setNextBooking(new BookingInfoDto(
                        nextBooking.getId(),
                        nextBooking.getBooker().getId(),
                        nextBooking.getStart(),
                        nextBooking.getEnd()
                ));
            }
        }

        return itemWithBookingsDto;
    }

    @Override
    public List<ItemWithBookingsDto> getByOwnerId(Long ownerId) {
        userService.getById(ownerId);

        List<Item> items = itemRepository.findByOwnerIdOrderById(ownerId);

        return items.stream().map(item -> {
            List<CommentResponseDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId())
                    .stream()
                    .map(CommentMapper::toCommentResponseDto)
                    .collect(Collectors.toList());

            ItemWithBookingsDto dto = new ItemWithBookingsDto();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setAvailable(item.getAvailable());
            dto.setRequestId(item.getRequestId());
            dto.setComments(comments);

            // Для владельца показываем информацию о бронированиях
            List<Booking> lastBookings = bookingRepository.findByItemIdAndEndBeforeOrderByEndDesc(
                    item.getId(), LocalDateTime.now());
            if (!lastBookings.isEmpty()) {
                Booking lastBooking = lastBookings.get(0);
                dto.setLastBooking(new BookingInfoDto(
                        lastBooking.getId(),
                        lastBooking.getBooker().getId(),
                        lastBooking.getStart(),
                        lastBooking.getEnd()
                ));
            }

            List<Booking> nextBookings = bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(
                    item.getId(), LocalDateTime.now());
            if (!nextBookings.isEmpty()) {
                Booking nextBooking = nextBookings.get(0);
                dto.setNextBooking(new BookingInfoDto(
                        nextBooking.getId(),
                        nextBooking.getBooker().getId(),
                        nextBooking.getStart(),
                        nextBooking.getEnd()
                ));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailableItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long itemId, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        // Проверяем существование пользователя и вещи
        User author = new User();
        author.setId(userId);
        userService.getById(userId); // проверяем что пользователь существует

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        // Проверяем, что пользователь действительно брал вещь в аренду
        List<Booking> userBookings = bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            throw new BadRequestException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        // Создаем комментарий
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentResponseDto(savedComment);
    }
}