package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("testUser");
        user.setEmail("test@user.ru");
    }

    @Test
    void addUser_whenUserIsValid_thenReturnsSavedUser() throws ValidationException, ConflictException {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.addUser(user);

        assertNotNull(savedUser);
        assertEquals(user.getName(), savedUser.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void addUser_whenEmailIsBlank_thenThrowsValidationException() {
        user.setEmail("");
        assertThrows(ValidationException.class, () -> userService.addUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUser_whenEmailAlreadyExists_thenThrowsConflictException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertThrows(ConflictException.class, () -> userService.addUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenUserExists_thenReturnsUpdatedUser() throws ConflictException {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("updatedUser");
        updatedUser.setEmail("updated@user.ru");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(updatedUser);

        assertNotNull(result);
        assertEquals(updatedUser.getName(), result.getName());
        assertEquals(updatedUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_whenUserNotFound_thenThrowsNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenEmailConflict_thenThrowsConflictException() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@user.ru");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("existing@user.ru");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@user.ru")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(updatedUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_whenUserExists_thenDeletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserNotFound_thenThrowsNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getUserById_whenUserExists_thenReturnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User foundUser = userService.getUserById(1L);
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_whenUserNotFound_thenThrowsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void getAllUsers_whenCalled_thenReturnsAllUsers() {
        List<User> users = List.of(user, new User());
        when(userRepository.findAll()).thenReturn(users);
        List<User> foundUsers = userService.getAllUsers();
        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
        verify(userRepository, times(1)).findAll();
    }
}