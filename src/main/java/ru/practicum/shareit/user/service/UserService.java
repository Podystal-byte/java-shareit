package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User addUser(User user) throws ConflictException, ValidationException {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым.");
        }

        if (userRepository.findAll().stream()
                .anyMatch(u -> Objects.equals(u.getEmail(), user.getEmail()))) {
            throw new ConflictException("Пользователь с таким email уже существует.");
        }

        return userRepository.create(user);
    }


    public User updateUser(User updatedUser) throws ConflictException {
        User existingUser = userRepository.findById(updatedUser.getId());
        if (existingUser == null) {
            throw new NotFoundException("User not found.");
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank() && !updatedUser.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.findAll().stream().anyMatch(u -> u.getEmail() != null && u.getEmail().equals(updatedUser.getEmail()))) {
                throw new ConflictException("User with this email already exists.");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
            existingUser.setName(updatedUser.getName());
        }

        log.info("Updating user with id: {}", updatedUser.getId());
        return userRepository.update(existingUser);
    }

    public void deleteUser(int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        log.info("Deleting user with id: {}", id);
        userRepository.delete(user);
    }

    public User getUserById(int id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        log.info("Fetching user with id: {}", id);
        return user;
    }

    public List<User> getAllUsers() {
        log.info("Fetching all users.");
        return userRepository.findAll();
    }
}