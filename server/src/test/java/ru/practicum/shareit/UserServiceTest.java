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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");

        user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
    }

    @Test
    public void addUser_Success() throws ValidationException, ConflictException {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        // When
        User savedUser = userService.addUser(user1);

        // Then
        assertNotNull(savedUser);
        assertEquals(user1.getName(), savedUser.getName());
        verify(userRepository, times(1)).existsByEmail(user1.getEmail());
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    public void addUser_EmailIsBlank_ThrowsValidationException() {
        // Given
        user1.setEmail(" ");

        // When & Then
        assertThrows(ValidationException.class, () -> userService.addUser(user1));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void addUser_EmailAlreadyExists_ThrowsConflictException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () -> userService.addUser(user1));
        verify(userRepository, times(1)).existsByEmail(user1.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void updateUser_UpdateNameAndEmail_Success() throws ConflictException {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("John Updated");
        updatedUser.setEmail("john.updated@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(updatedUser);

        // Then
        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("john.updated@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).existsByEmail(updatedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateUser_UserNotFound_ThrowsException() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(99L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.updateUser(updatedUser));
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void updateUser_EmailAlreadyExists_ThrowsConflictException() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("jane.smith@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class, () -> userService.updateUser(updatedUser));
        verify(userRepository, times(1)).findById(updatedUser.getId());
        verify(userRepository, times(1)).existsByEmail(updatedUser.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void deleteUser_Success() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        // When
        userService.deleteUser(user1.getId());

        // Then
        verify(userRepository, times(1)).existsById(user1.getId());
        verify(userRepository, times(1)).deleteById(user1.getId());
    }

    @Test
    public void deleteUser_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.deleteUser(user1.getId()));
        verify(userRepository, times(1)).existsById(user1.getId());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    public void getUserById_Success() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        // When
        User foundUser = userService.getUserById(user1.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(user1.getId(), foundUser.getId());
        verify(userRepository, times(1)).findById(user1.getId());
    }

    @Test
    public void getUserById_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    public void getAllUsers_Success() {
        // Given
        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> foundUsers = userService.getAllUsers();

        // Then
        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
        assertEquals(user1.getName(), foundUsers.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }
}
