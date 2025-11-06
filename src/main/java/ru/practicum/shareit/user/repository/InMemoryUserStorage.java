package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        return users.get(id);
    }

    @Override
    public User create(User user) {
        if (existsByEmailAndId(user.getEmail(), null)) {
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (existsByEmailAndId(user.getEmail(), user.getId())) {
            throw new ValidationException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    private boolean existsByEmailAndId(String email, Long excludeId) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equals(email) &&
                        (excludeId == null || !user.getId().equals(excludeId)));
    }
}
