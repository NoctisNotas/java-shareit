package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
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
class ItemServiceIntegrationTest {

    private final ItemService itemService;
    private final UserService userService;
    private final EntityManager entityManager;

    private UserDto owner;
    private UserDto booker;
    private Long ownerId;
    private Long bookerId;

    @BeforeEach
    void setUp() {
        owner = userService.create(new UserDto(null, "Owner", "owner@example.com"));
        booker = userService.create(new UserDto(null, "Booker", "booker@example.com"));
        ownerId = owner.getId();
        bookerId = booker.getId();
    }

    @Test
    void testCreateItemSuccess() {
        ItemDto itemDto = new ItemDto(null, "Hammer", "Heavy hammer", true, null);

        ItemDto createdItem = itemService.create(itemDto, ownerId);

        assertThat(createdItem.getId(), notNullValue());
        assertThat(createdItem.getName(), equalTo("Hammer"));
        assertThat(createdItem.getDescription(), equalTo("Heavy hammer"));
        assertThat(createdItem.getAvailable(), equalTo(true));

        TypedQuery<Item> query = entityManager.createQuery("SELECT i FROM Item i WHERE i.name = :name", Item.class);
        Item savedItem = query.setParameter("name", "Hammer").getSingleResult();

        assertThat(savedItem.getName(), equalTo("Hammer"));
        assertThat(savedItem.getOwner().getId(), equalTo(ownerId));
    }

    @Test
    void testCreateItemNonExistingOwnerThrowsNotFoundException() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(itemDto, 999L));

        assertThat(exception.getMessage(), containsString("Пользователь с id 999 не найден"));
    }

    @Test
    void testUpdateItemSuccess() {
        ItemDto itemDto = new ItemDto(null, "Old Name", "Old Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);

        ItemDto updateDto = new ItemDto(createdItem.getId(), "New Name", "New Description", false, null);
        ItemDto updatedItem = itemService.update(updateDto, ownerId);

        assertThat(updatedItem.getName(), equalTo("New Name"));
        assertThat(updatedItem.getDescription(), equalTo("New Description"));
        assertThat(updatedItem.getAvailable(), equalTo(false));
    }

    @Test
    void testUpdateItemNotOwnerThrowsNotFoundException() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);

        ItemDto updateDto = new ItemDto(createdItem.getId(), "New Name", "New Description", false, null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(updateDto, bookerId));

        assertThat(exception.getMessage(), containsString("Пользователь не является владельцем вещи"));
    }

    @Test
    void testGetByIdWithBookingsAndComments() {
        ItemDto itemDto = new ItemDto(null, "Test Item", "Test Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(entityManager.find(Item.class, createdItem.getId()));
        booking.setBooker(entityManager.find(User.class, bookerId));
        booking.setStatus(BookingStatus.APPROVED);
        entityManager.persist(booking);
        entityManager.flush();

        CommentDto commentDto = new CommentDto("Great item!");
        itemService.addComment(createdItem.getId(), commentDto, bookerId);

        ItemWithBookingsDto itemWithBookings = itemService.getById(createdItem.getId(), ownerId);

        assertThat(itemWithBookings.getId(), equalTo(createdItem.getId()));
        assertThat(itemWithBookings.getName(), equalTo("Test Item"));
        assertThat(itemWithBookings.getLastBooking(), notNullValue());
        assertThat(itemWithBookings.getComments(), hasSize(1));
        assertThat(itemWithBookings.getComments().get(0).getText(), equalTo("Great item!"));
    }

    @Test
    void testGetByOwnerIdReturnsItems() {
        ItemDto item1 = new ItemDto(null, "Item 1", "Description 1", true, null);
        ItemDto item2 = new ItemDto(null, "Item 2", "Description 2", true, null);

        itemService.create(item1, ownerId);
        itemService.create(item2, ownerId);

        List<ItemWithBookingsDto> items = itemService.getByOwnerId(ownerId);

        assertThat(items, hasSize(2));
        assertThat(items.get(0).getName(), equalTo("Item 1"));
        assertThat(items.get(1).getName(), equalTo("Item 2"));
    }

    @Test
    void testSearchAvailableItems() {
        ItemDto availableItem = new ItemDto(null, "Available Item", "Searchable text", true, null);
        ItemDto unavailableItem = new ItemDto(null, "Unavailable Item", "Searchable text", false, null);

        itemService.create(availableItem, ownerId);
        itemService.create(unavailableItem, ownerId);

        List<ItemDto> searchResults = itemService.search("searchable");

        assertThat(searchResults, hasSize(1));
        assertThat(searchResults.get(0).getName(), equalTo("Available Item"));
    }

    @Test
    void testSearchEmptyTextReturnsEmptyList() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        itemService.create(itemDto, ownerId);

        List<ItemDto> searchResults = itemService.search("");

        assertThat(searchResults, empty());
    }

    @Test
    void testAddCommentSuccess() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setItem(entityManager.find(Item.class, createdItem.getId()));
        booking.setBooker(entityManager.find(User.class, bookerId));
        booking.setStatus(BookingStatus.APPROVED);
        entityManager.persist(booking);
        entityManager.flush();

        CommentDto commentDto = new CommentDto("Excellent item!");
        var commentResponse = itemService.addComment(createdItem.getId(), commentDto, bookerId);

        assertThat(commentResponse.getId(), notNullValue());
        assertThat(commentResponse.getText(), equalTo("Excellent item!"));
        assertThat(commentResponse.getAuthorName(), equalTo("Booker"));
        assertThat(commentResponse.getCreated(), notNullValue());
    }

    @Test
    void testAddCommentNoBookingThrowsBadRequestException() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);

        CommentDto commentDto = new CommentDto("Comment");

        Exception exception = assertThrows(Exception.class,
                () -> itemService.addComment(createdItem.getId(), commentDto, bookerId));

        assertThat(exception.getMessage(), containsString("Пользователь не брал вещь в аренду"));
    }

    @Test
    void testDeleteItemSuccess() {
        ItemDto itemDto = new ItemDto(null, "To Delete", "Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);

        itemService.delete(createdItem.getId(), ownerId);

        TypedQuery<Item> query = entityManager.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        List<Item> items = query.setParameter("id", createdItem.getId()).getResultList();

        assertThat(items, empty());
    }

    @Test
    void testDeleteItemNotOwnerThrowsNotFoundException() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        ItemDto createdItem = itemService.create(itemDto, ownerId);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.delete(createdItem.getId(), bookerId));

        assertThat(exception.getMessage(), containsString("Пользователь не является владельцем вещи"));
    }
}