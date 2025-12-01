package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(BookingDto bookingDto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + bookingDto.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new BadRequestException("Некорректные даты бронирования");
        }

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateStatus(Long bookingId, Boolean approved, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new BadRequestException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Доступ к бронированию запрещен");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, int from, int size) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с id " + ownerId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdCurrentOrderByStartDesc(ownerId, now, pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now, pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now, pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}