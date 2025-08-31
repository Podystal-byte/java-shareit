package ru.practicum.shareit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repo.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void findByEmail_whenEmailExists_shouldReturnUser() {
        User found = userRepository.findByEmail("test@example.com");
        assertNotNull(found);
        assertEquals("Test User", found.getName());
    }

    @Test
    void findByEmail_whenEmailDoesNotExist_shouldReturnNull() {
        User found = userRepository.findByEmail("nonexistent@example.com");
        assertNull(found);
    }
}
