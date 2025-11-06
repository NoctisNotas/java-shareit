package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import java.util.*;

@Component
public class InMemoryUserStorage implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public Collection<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    @Override
    public User create(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }

        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        User existingUser = getById(user.getId());

        if (!existingUser.getEmail().equals(user.getEmail()) && existsByEmail(user.getEmail())) {
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }

        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        getById(id);
        users.remove(id);
    }

    private boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }
}