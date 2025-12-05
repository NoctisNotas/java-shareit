package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationTest {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private final EntityManager entityManager;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.create(new UserDto(null, "Owner", "owner@example.com"));
        UserDto booker = userService.create(new UserDto(null, "Booker", "booker@example.com"));
        ownerId = owner.getId();
        bookerId = booker.getId();

        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);
        itemId = createdItem.getId();
    }

    @Test
    void testCreateBookingSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");

        BookingResponseDto createdBooking = bookingService.create(bookingDto, bookerId);

        assertThat(createdBooking.getId(), notNullValue());
        assertThat(createdBooking.getStart(), equalTo(start));
        assertThat(createdBooking.getEnd(), equalTo(end));
        assertThat(createdBooking.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(createdBooking.getBooker().getId(), equalTo(bookerId));
        assertThat(createdBooking.getItem().getId(), equalTo(itemId));
    }

    @Test
    void testCreateBookingOwnerCantBookOwnItemThrowsNotFoundException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingDto, ownerId));

        assertThat(exception.getMessage(), containsString("Владелец не может бронировать свою вещь"));
    }

    @Test
    void testCreateBookingItemNotAvailableThrowsBadRequestException() {
        ItemDto unavailableItem = new ItemDto(null, "Unavailable", "Description", false, null);
        ItemDto createdItem = itemService.create(unavailableItem, ownerId);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, createdItem.getId(), "WAITING");

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> bookingService.create(bookingDto, bookerId));

        assertThat(exception.getMessage(), containsString("Вещь недоступна для бронирования"));
    }

    @Test
    void testCreateBookingInvalidDatesThrowsBadRequestException() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");

        BadRequestException exception = assertThrows(
                BadRequestException.class, () -> bookingService.create(bookingDto, bookerId));

        assertThat(exception.getMessage(), containsString("Некорректные даты бронирования"));
    }

    @Test
    void testUpdateStatusApproveSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");
        BookingResponseDto createdBooking = bookingService.create(bookingDto, bookerId);

        BookingResponseDto updatedBooking = bookingService.updateStatus(createdBooking.getId(), true, ownerId);

        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void testUpdateStatusRejectSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");
        BookingResponseDto createdBooking = bookingService.create(bookingDto, bookerId);

        BookingResponseDto updatedBooking = bookingService.updateStatus(createdBooking.getId(), false, ownerId);

        assertThat(updatedBooking.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void testUpdateStatusNotOwnerThrowsBadRequestException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");
        BookingResponseDto createdBooking = bookingService.create(bookingDto, bookerId);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.updateStatus(createdBooking.getId(), true, bookerId));

        assertThat(exception.getMessage(), containsString("Только владелец вещи может подтверждать бронирование"));
    }

    @Test
    void testUpdateStatusAlreadyApprovedThrowsBadRequestException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");
        BookingResponseDto createdBooking = bookingService.create(bookingDto, bookerId);
        bookingService.updateStatus(createdBooking.getId(), true, ownerId);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.updateStatus(createdBooking.getId(), true, ownerId));

        assertThat(exception.getMessage(), containsString("Статус бронирования уже изменен"));
    }

    @Test
    void testGetByIdSuccess() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");
        BookingResponseDto createdBooking = bookingService.create(bookingDto, bookerId);

        BookingResponseDto foundBooking = bookingService.getById(createdBooking.getId(), bookerId);

        assertThat(foundBooking.getId(), equalTo(createdBooking.getId()));
        assertThat(foundBooking.getBooker().getId(), equalTo(bookerId));
    }

    @Test
    void testGetByIdNotAllowedThrowsNotFoundException() {
        UserDto anotherUser = userService.create(new UserDto(null, "Another", "another@example.com"));

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");
        BookingResponseDto createdBooking = bookingService.create(bookingDto, bookerId);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(createdBooking.getId(), anotherUser.getId()));

        assertThat(exception.getMessage(), containsString("Доступ к бронированию запрещен"));
    }

    @Test
    void testGetUserBookingsAllState() {
        createTestBooking(itemId, bookerId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        createTestBooking(itemId, bookerId, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4));

        List<BookingResponseDto> bookings = bookingService.getUserBookings(bookerId, "ALL", 0, 10);

        assertThat(bookings, hasSize(2));
    }

    @Test
    void testGetUserBookingsWaitingState() {
        createTestBooking(itemId, bookerId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(3));
        booking.setEnd(LocalDateTime.now().plusDays(4));
        booking.setItem(entityManager.find(Item.class, itemId));
        booking.setBooker(entityManager.find(User.class, bookerId));
        booking.setStatus(BookingStatus.APPROVED);
        entityManager.persist(booking);
        entityManager.flush();

        List<BookingResponseDto> bookings = bookingService.getUserBookings(bookerId, "WAITING", 0, 10);

        assertThat(bookings, hasSize(1));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }

    @Test
    void testGetOwnerBookingsAllState() {
        createTestBooking(itemId, bookerId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        ItemDto anotherItem = new ItemDto(null, "Another Item", "Description", true, null);
        ItemDto createdAnotherItem = itemService.create(anotherItem, ownerId);
        createTestBooking(createdAnotherItem.getId(), bookerId, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4));

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId, "ALL", 0, 10);

        assertThat(bookings, hasSize(2));
    }

    @Test
    void testGetOwnerBookingsRejectedState() {
        createTestBooking(itemId, bookerId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(3));
        booking.setEnd(LocalDateTime.now().plusDays(4));
        booking.setItem(entityManager.find(Item.class, itemId));
        booking.setBooker(entityManager.find(User.class, bookerId));
        booking.setStatus(BookingStatus.REJECTED);
        entityManager.persist(booking);
        entityManager.flush();

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId, "REJECTED", 0, 10);

        assertThat(bookings, hasSize(1));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void testGetOwnerBookingsInvalidStateThrowsBadRequestException() {
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.getOwnerBookings(ownerId, "INVALID", 0, 10));

        assertThat(exception.getMessage(), containsString("Unknown state: INVALID"));
    }

    private void createTestBooking(Long itemId, Long bookerId, LocalDateTime start, LocalDateTime end) {
        BookingDto bookingDto = new BookingDto(null, start, end, itemId, "WAITING");
        bookingService.create(bookingDto, bookerId);
    }
}