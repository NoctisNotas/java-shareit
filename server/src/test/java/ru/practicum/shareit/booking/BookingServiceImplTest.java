package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");
        item = new Item(1L, "Drill", "Description", true, owner, null);
        booking = new Booking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING);
        bookingDto = new BookingDto(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), 1L, "WAITING");
    }

    @Test
    void createValidBookingReturnsBookingResponse() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.create(bookingDto, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createOwnerBooksOwnItemThrowsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> bookingService.create(bookingDto, 1L));

        assertEquals("Владелец не может бронировать свою вещь", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createItemNotAvailableThrowsBadRequestException() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> bookingService.create(bookingDto, 2L));

        assertEquals("Вещь недоступна для бронирования", exception.getMessage());
    }

    @Test
    void createInvalidDatesThrowsBadRequestException() {
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> bookingService.create(bookingDto, 2L));

        assertEquals("Некорректные даты бронирования", exception.getMessage());
    }

    @Test
    void updateStatusApproveSuccess() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.updateStatus(1L, true, 1L);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateStatusNotOwnerThrowsBadRequestException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> bookingService.updateStatus(1L, true, 2L));

        assertEquals("Только владелец вещи может подтверждать бронирование", exception.getMessage());
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateStatusAlreadyApprovedThrowsBadRequestException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> bookingService.updateStatus(1L, true, 1L));

        assertEquals("Статус бронирования уже изменен", exception.getMessage());
    }

    @Test
    void getByIdValidBookingReturnsBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getById(1L, 2L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).findById(1L);
    }

    @Test
    void getByIdNotAllowedThrowsNotFoundException() {
        User anotherUser = new User(3L, "Another", "another@example.com");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> bookingService.getById(1L, 3L));

        assertEquals("Доступ к бронированию запрещен", exception.getMessage());
    }

    @Test
    void getUserBookingsAllStateReturnsBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).existsById(2L);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(eq(2L), any(Pageable.class));
    }

    @Test
    void getUserBookingsWaitingStateReturnsBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                eq(2L), eq(BookingStatus.WAITING), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, "WAITING", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(
                eq(2L), eq(BookingStatus.WAITING), any(Pageable.class));
    }

    @Test
    void getUserBookingsInvalidStateThrowsBadRequestException() {
        when(userRepository.existsById(2L)).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> bookingService.getUserBookings(2L, "INVALID", 0, 10));

        assertEquals("Unknown state: INVALID", exception.getMessage());
    }

    @Test
    void getOwnerBookingsAllStateReturnsBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).existsById(1L);
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(eq(1L), any(Pageable.class));
    }

    @Test
    void getOwnerBookingsEmptyListReturnsEmptyList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(1L), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "ALL", 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}