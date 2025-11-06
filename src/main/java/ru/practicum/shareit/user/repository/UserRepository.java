package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    Collection<User> getAll();

    User getById(Long id);

    User create(User user);

    User update(User user);

    void delete(Long id);
}
