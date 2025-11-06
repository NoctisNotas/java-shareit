package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Collection<UserDto> getAll() {
        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.getById(id);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User createdUser = userRepository.create(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto update(UserDto userDto) {
        User userToUpdate = new User();
        userToUpdate.setId(userDto.getId());
        userToUpdate.setName(userDto.getName()); // может быть null
        userToUpdate.setEmail(userDto.getEmail()); // может быть null

        User updatedUser = userRepository.update(userToUpdate);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }
}