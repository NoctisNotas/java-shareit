package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final ItemRequestService requestService;
    private final UserService userService;
    private final ItemService itemService;
    private final EntityManager entityManager;

    private Long requestorId;
    private Long anotherUserId;

    @BeforeEach
    void setUp() {
        UserDto requestor = userService.create(new UserDto(null, "Requestor", "requestor@example.com"));
        UserDto anotherUser = userService.create(new UserDto(null, "Another User", "another@example.com"));
        requestorId = requestor.getId();
        anotherUserId = anotherUser.getId();
    }

    @Test
    void testCreateItemRequestSuccess() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a drill for home repairs", null, null);

        ItemRequestDto createdRequest = requestService.create(requestDto, requestorId);

        assertThat(createdRequest.getId(), notNullValue());
        assertThat(createdRequest.getDescription(), equalTo("Need a drill for home repairs"));
        assertThat(createdRequest.getRequestorId(), equalTo(requestorId));
        assertThat(createdRequest.getCreated(), notNullValue());
    }

    @Test
    void testCreateItemRequestNonExistingUserThrowsNotFoundException() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Description", null, null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.create(requestDto, 999L));

        assertThat(exception.getMessage(), containsString("Пользователь с id 999 не найден"));
    }

    @Test
    void testGetUserRequestsWithItems() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need a tool", null, null);
        ItemRequestDto createdRequest = requestService.create(requestDto, requestorId);

        ItemDto itemDto = new ItemDto(null, "Hammer", "Tool", true, createdRequest.getId());
        itemService.create(itemDto, anotherUserId);

        List<ItemRequestResponseDto> requests = requestService.getUserRequests(requestorId);

        assertThat(requests, hasSize(1));
        assertThat(requests.get(0).getItems(), hasSize(1));
        assertThat(requests.get(0).getItems().get(0).getName(), equalTo("Hammer"));
    }

    @Test
    void testGetRequestByIdSuccess() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need specific tool", null, null);
        ItemRequestDto createdRequest = requestService.create(requestDto, requestorId);

        ItemRequestResponseDto foundRequest = requestService.getRequestById(createdRequest.getId(), requestorId);

        assertThat(foundRequest.getId(), equalTo(createdRequest.getId()));
        assertThat(foundRequest.getDescription(), equalTo("Need specific tool"));
        assertThat(foundRequest.getCreated(), notNullValue());
        assertThat(foundRequest.getItems(), empty());
    }

    @Test
    void testGetRequestByIdWithItems() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need tools", null, null);
        ItemRequestDto createdRequest = requestService.create(requestDto, requestorId);

        ItemDto item1 = new ItemDto(null, "Hammer", "Tool 1", true, createdRequest.getId());
        ItemDto item2 = new ItemDto(null, "Screwdriver", "Tool 2", true, createdRequest.getId());

        itemService.create(item1, anotherUserId);
        itemService.create(item2, anotherUserId);

        ItemRequestResponseDto foundRequest = requestService.getRequestById(createdRequest.getId(), requestorId);

        assertThat(foundRequest.getItems(), hasSize(2));
        assertThat(foundRequest.getItems().get(0).getName(), equalTo("Hammer"));
        assertThat(foundRequest.getItems().get(1).getName(), equalTo("Screwdriver"));
    }

    @Test
    void testGetRequestByIdNonExistingRequestThrowsNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(999L, requestorId));

        assertThat(exception.getMessage(), containsString("Запрос с id 999 не найден"));
    }

    @Test
    void testGetRequestByIdNonExistingUserThrowsNotFoundException() {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Description", null, null);
        ItemRequestDto createdRequest = requestService.create(requestDto, requestorId);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(createdRequest.getId(), 999L));

        assertThat(exception.getMessage(), containsString("Пользователь с id 999 не найден"));
    }

    @Test
    void testGetUserRequestsEmptyList() {
        List<ItemRequestResponseDto> requests = requestService.getUserRequests(requestorId);
        assertThat(requests, empty());
    }

    @Test
    void testGetAllRequestsEmptyList() {
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(requestorId, 0, 10);
        assertThat(requests, empty());
    }
}