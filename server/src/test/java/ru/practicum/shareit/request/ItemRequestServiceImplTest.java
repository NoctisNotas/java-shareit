package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
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
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User requestor;
    private ItemRequest request;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestor = new User(1L, "Requestor", "requestor@example.com");
        request = new ItemRequest(1L, "Need a drill", requestor, LocalDateTime.now());
        requestDto = new ItemRequestDto(null, "Need a drill", null, null);
    }

    @Test
    void createValidRequestReturnsItemRequestDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto result = requestService.create(requestDto, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        assertEquals(1L, result.getRequestorId());
        verify(userRepository).findById(1L);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createNonExistingUserThrowsNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.create(requestDto, 999L));

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(requestRepository, never()).save(any());
    }

    @Test
    void getUserRequestsReturnsRequestsWithItems() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(Collections.emptyList());

        List<ItemRequestResponseDto> result = requestService.getUserRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Need a drill", result.get(0).getDescription());
        verify(userRepository).existsById(1L);
        verify(requestRepository).findByRequestorIdOrderByCreatedDesc(1L);
        verify(itemRepository).findByRequestId(1L);
    }

    @Test
    void getUserRequestsNonExistingUserThrowsNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getUserRequests(999L));

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository).existsById(999L);
        verify(requestRepository, never()).findByRequestorIdOrderByCreatedDesc(any());
    }

    @Test
    void getAllRequestsReturnsOtherUsersRequests() {
        User anotherUser = new User(2L, "Another", "another@example.com");
        when(userRepository.existsById(2L)).thenReturn(true);
        when(requestRepository.findByRequestorIdNotOrderByCreatedDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(Collections.emptyList());

        List<ItemRequestResponseDto> result = requestService.getAllRequests(2L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Need a drill", result.get(0).getDescription());
        verify(userRepository).existsById(2L);
        verify(requestRepository).findByRequestorIdNotOrderByCreatedDesc(eq(2L), any(Pageable.class));
    }

    @Test
    void getAllRequestsWithItemsReturnsRequestsWithItems() {
        Item item = new Item(1L, "Drill", "Description", true, requestor, 1L);
        when(userRepository.existsById(2L)).thenReturn(true);
        when(requestRepository.findByRequestorIdNotOrderByCreatedDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = requestService.getAllRequests(2L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals("Drill", result.get(0).getItems().get(0).getName());
    }

    @Test
    void getRequestByIdReturnsRequestWithItems() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestId(1L)).thenReturn(Collections.emptyList());

        ItemRequestResponseDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        verify(userRepository).existsById(1L);
        verify(requestRepository).findById(1L);
        verify(itemRepository).findByRequestId(1L);
    }

    @Test
    void getRequestByIdNonExistingRequestThrowsNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(999L, 1L));

        assertEquals("Запрос с id 999 не найден", exception.getMessage());
        verify(requestRepository).findById(999L);
    }

    @Test
    void getRequestByIdNonExistingUserThrowsNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(1L, 999L));

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository).existsById(999L);
        verify(requestRepository, never()).findById(any());
    }
}