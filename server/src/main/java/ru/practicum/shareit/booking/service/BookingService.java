package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingDto bookingDto, Long userId);

    BookingResponseDto updateStatus(Long bookingId, Boolean approved, Long userId);

    BookingResponseDto getById(Long bookingId, Long userId);

    List<BookingResponseDto> getUserBookings(Long userId, String state, int from, int size);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, int from, int size);
}