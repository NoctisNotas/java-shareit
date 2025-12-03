package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateAndGetUser() {
        UserDto newUser = new UserDto(null, "Test User", "test@example.com");
        UserDto savedUser = userService.create(newUser);

        assertNotNull(savedUser.getId());

        UserDto foundUser = userService.getById(savedUser.getId());

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("Test User", foundUser.getName());
        assertEquals("test@example.com", foundUser.getEmail());
    }
}