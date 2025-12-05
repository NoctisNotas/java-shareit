package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");
        item = new Item(1L, "Drill", "Heavy duty drill", true, owner, null);
        itemDto = new ItemDto(1L, "Drill", "Heavy duty drill", true, null);
    }

    @Test
    void createValidItemReturnsItemDto() {
        when(userService.getById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@example.com"));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.create(itemDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Drill", result.getName());
        verify(userService).getById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateValidUpdateReturnsUpdatedItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto updateDto = new ItemDto(1L, "Updated Drill", "Updated description", false, null);
        ItemDto result = itemService.update(updateDto, 1L);

        assertNotNull(result);
        assertEquals("Updated Drill", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateNotOwnerThrowsNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto updateDto = new ItemDto(1L, "Updated", "Updated", true, null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(updateDto, 2L));

        assertEquals("Пользователь не является владельцем вещи", exception.getMessage());
        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getByIdReturnsItemWithBookingsAndComments() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemIdAndEndBeforeOrderByEndDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        ItemWithBookingsDto result = itemService.getById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Drill", result.getName());
        verify(itemRepository).findById(1L);
        verify(commentRepository).findByItemIdOrderByCreatedDesc(1L);
    }

    @Test
    void getByOwnerIdReturnsItemsList() {
        when(userService.getById(1L)).thenReturn(new UserDto(1L, "Owner", "owner@example.com"));
        when(itemRepository.findByOwnerIdOrderById(1L)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemIdAndEndBeforeOrderByEndDesc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemIdAndStartAfterOrderByStartAsc(eq(1L), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<ItemWithBookingsDto> result = itemService.getByOwnerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
        verify(userService).getById(1L);
        verify(itemRepository).findByOwnerIdOrderById(1L);
    }

    @Test
    void searchValidTextReturnsAvailableItems() {
        when(itemRepository.searchAvailableItems("drill")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.search("drill");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
        verify(itemRepository).searchAvailableItems("drill");
    }

    @Test
    void searchEmptyTextReturnsEmptyList() {
        List<ItemDto> result = itemService.search("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(any());
    }

    @Test
    void addCommentValidCommentReturnsCommentResponse() {
        when(userService.getById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@example.com"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(1L), eq(2L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(List.of(new Booking()));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            comment.setCreated(LocalDateTime.now());
            return comment;
        });

        CommentDto commentDto = new CommentDto("Great item!");
        var result = itemService.addComment(1L, commentDto, 2L);

        assertNotNull(result);
        assertEquals("Great item!", result.getText());
        assertEquals("Booker", result.getAuthorName());
        verify(userService).getById(2L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).findByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(1L), eq(2L), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addCommentNoBookingThrowsBadRequestException() {
        when(userService.getById(2L)).thenReturn(new UserDto(2L, "Booker", "booker@example.com"));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(1L), eq(2L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        CommentDto commentDto = new CommentDto("Comment");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> itemService.addComment(1L, commentDto, 2L));

        assertEquals("Пользователь не брал вещь в аренду или аренда еще не завершена", exception.getMessage());
        verify(bookingRepository).findByItemIdAndBookerIdAndStatusAndEndBefore(
                eq(1L), eq(2L), eq(BookingStatus.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void deleteValidItemDeletesSuccessfully() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L, 1L);

        verify(itemRepository).findById(1L);
        verify(itemRepository).deleteById(1L);
    }
}