package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.dto.UserMapperImpl;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {UserMapperImpl.class})
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void toDto() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        // When
        UserDto userDto = userMapper.toDto(user);

        // Then
        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    void toDto_whenUserIsNull_shouldReturnNull() {
        // When
        UserDto userDto = userMapper.toDto(null);

        // Then
        assertNull(userDto);
    }


    @Test
    void toUser() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Jane Doe");
        userDto.setEmail("jane.doe@example.com");

        // When
        User user = userMapper.toUser(userDto);

        // Then
        assertNotNull(user);
        assertEquals(userDto.getId(), user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    void toUser_whenUserDtoIsNull_shouldReturnNull() {
        // When
        User user = userMapper.toUser(null);

        // Then
        assertNull(user);
    }
}
