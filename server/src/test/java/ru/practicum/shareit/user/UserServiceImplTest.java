package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John Doe", "john@example.com");
        userDto = new UserDto(1L, "John Doe", "john@example.com");
    }

    @Test
    void getByIdExistingIdReturnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void getByIdNonExistingIdThrowsNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getById(999L));

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getAllReturnsAllUsers() {
        List<User> users = List.of(
                new User(1L, "User1", "user1@example.com"),
                new User(2L, "User2", "user2@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        Collection<UserDto> result = userService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void createValidUserReturnsCreatedUser() {
        UserDto newUserDto = new UserDto(null, "New User", "new@example.com");
        User newUser = UserMapper.toUser(newUserDto);
        newUser.setId(3L);

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        UserDto result = userService.create(newUserDto);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New User", result.getName());
        assertEquals("new@example.com", result.getEmail());

        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createDuplicateEmailThrowsValidationException() {
        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(user));

        UserDto duplicateUser = new UserDto(null, "Another", "existing@example.com");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.create(duplicateUser));

        assertEquals("Пользователь с email existing@example.com уже существует", exception.getMessage());
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateValidUpdateReturnsUpdatedUser() {
        UserDto updateDto = new UserDto(1L, "Updated Name", "updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("updated@example.com", 1L))
                .thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDto result = userService.update(updateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmailAndIdNot("updated@example.com", 1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateNonExistingUserThrowsNotFoundException() {
        UserDto updateDto = new UserDto(999L, "Updated", "updated@example.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.update(updateDto));

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteExistingUserDeletesSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteNonExistingUserThrowsNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.delete(999L));

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(any());
    }
}