package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {

    private final UserService userService;
    private final EntityManager entityManager;

    @Test
    void testCreateUserSuccess() {
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");

        UserDto createdUser = userService.create(userDto);

        assertThat(createdUser.getId(), notNullValue());
        assertThat(createdUser.getName(), equalTo("John Doe"));
        assertThat(createdUser.getEmail(), equalTo("john@example.com"));

        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
        User savedUser = query.setParameter("email", "john@example.com").getSingleResult();

        assertThat(savedUser.getId(), equalTo(createdUser.getId()));
        assertThat(savedUser.getName(), equalTo("John Doe"));
        assertThat(savedUser.getEmail(), equalTo("john@example.com"));
    }

    @Test
    void testCreateUserDuplicateEmailThrowsValidationException() {
        UserDto firstUser = new UserDto(null, "Peter Parker", "peter@example.com");
        userService.create(firstUser);

        UserDto secondUser = new UserDto(null, "Peter Parker 2", "peter@example.com");
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.create(secondUser));

        assertThat(exception.getMessage(),
                containsString("Пользователь с email peter@example.com уже существует"));
    }

    @Test
    void testGetByIdExistingUserReturnsUser() {
        UserDto userDto = new UserDto(null, "Alice Smith", "alice@example.com");
        UserDto createdUser = userService.create(userDto);

        UserDto foundUser = userService.getById(createdUser.getId());

        assertThat(foundUser.getId(), equalTo(createdUser.getId()));
        assertThat(foundUser.getName(), equalTo("Alice Smith"));
        assertThat(foundUser.getEmail(), equalTo("alice@example.com"));
    }

    @Test
    void testGetByIdNonExistingUserThrowsNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getById(999L));

        assertThat(exception.getMessage(), containsString("Пользователь с id 999 не найден"));
    }

    @Test
    void testGetAllReturnsAllUsers() {
        UserDto user1 = new UserDto(null, "User One", "user1@example.com");
        UserDto user2 = new UserDto(null, "User Two", "user2@example.com");

        userService.create(user1);
        userService.create(user2);

        Collection<UserDto> users = userService.getAll();

        assertThat(users, hasSize(greaterThanOrEqualTo(2)));
        assertThat(users, hasItem(hasProperty("email", equalTo("user1@example.com"))));
        assertThat(users, hasItem(hasProperty("email", equalTo("user2@example.com"))));
    }

    @Test
    void testUpdateUserSuccess() {
        UserDto originalUser = new UserDto(null, "Original Name", "original@example.com");
        UserDto createdUser = userService.create(originalUser);

        UserDto updateDto = new UserDto(createdUser.getId(), "Updated Name", "updated@example.com");
        UserDto updatedUser = userService.update(updateDto);

        assertThat(updatedUser.getId(), equalTo(createdUser.getId()));
        assertThat(updatedUser.getName(), equalTo("Updated Name"));
        assertThat(updatedUser.getEmail(), equalTo("updated@example.com"));

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User savedUser = query.setParameter("id", createdUser.getId()).getSingleResult();

        assertThat(savedUser.getName(), equalTo("Updated Name"));
        assertThat(savedUser.getEmail(), equalTo("updated@example.com"));
    }

    @Test
    void testUpdateUserPartialUpdate() {
        UserDto originalUser = new UserDto(null, "Original Name", "original@example.com");
        UserDto createdUser = userService.create(originalUser);

        UserDto updateDto = new UserDto(createdUser.getId(), "Updated Name", null);
        UserDto updatedUser = userService.update(updateDto);

        assertThat(updatedUser.getId(), equalTo(createdUser.getId()));
        assertThat(updatedUser.getName(), equalTo("Updated Name"));
        assertThat(updatedUser.getEmail(), equalTo("original@example.com"));
    }

    @Test
    void testUpdateUserNonExistingUserThrowsNotFoundException() {
        UserDto updateDto = new UserDto(999L, "Updated", "updated@example.com");
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.update(updateDto));

        assertThat(exception.getMessage(), containsString("Пользователь с id 999 не найден"));
    }

    @Test
    void testUpdateUserDuplicateEmailThrowsValidationException() {
        UserDto user1 = new UserDto(null, "User One", "user1@example.com");
        UserDto user2 = new UserDto(null, "User Two", "user2@example.com");

        UserDto createdUser1 = userService.create(user1);
        userService.create(user2);
        UserDto updateDto = new UserDto(createdUser1.getId(), "User One", "user2@example.com");
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.update(updateDto));

        assertThat(exception.getMessage(),
                containsString("Пользователь с email user2@example.com уже существует"));
    }

    @Test
    void testDeleteUserSuccess() {
        UserDto userDto = new UserDto(null, "To Delete", "delete@example.com");
        UserDto createdUser = userService.create(userDto);

        userService.delete(createdUser.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(createdUser.getId()));
    }

    @Test
    void testDeleteUserNonExistingUserThrowsNotFoundException() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.delete(999L));

        assertThat(exception.getMessage(), containsString("Пользователь с id 999 не найден"));
    }

    @Test
    void testFindByEmailSuccess() {
        UserDto userDto = new UserDto(null, "Test User", "test@example.com");
        userService.create(userDto);

        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
        User foundUser = query.setParameter("email", "test@example.com").getSingleResult();

        assertThat(foundUser.getName(), equalTo("Test User"));
        assertThat(foundUser.getEmail(), equalTo("test@example.com"));
    }
}