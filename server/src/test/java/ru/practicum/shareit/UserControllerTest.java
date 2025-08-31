package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("testUser");
        user.setEmail("test@user.ru");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("testUser");
        userDto.setEmail("test@user.ru");
    }

    @Test
    void getUser_whenUserExists_thenReturnsUserDto() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        verify(userService, times(1)).getUserById(1L);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void getUser_whenUserNotFound_thenReturns404() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new NotFoundException("User not found."));

        mockMvc.perform(get("/users/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(99L);
    }

    @Test
    void createUser_whenValidUser_thenReturns201() throws Exception {
        when(userMapper.toUser(any(UserDto.class))).thenReturn(user);
        when(userService.addUser(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()));

        verify(userService, times(1)).addUser(any(User.class));
    }

    @Test
    void createUser_whenEmailConflict_thenReturns409() throws Exception {
        when(userMapper.toUser(any(UserDto.class))).thenReturn(user);
        when(userService.addUser(any(User.class))).thenThrow(new ConflictException("Пользователь с таким email уже существует."));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).addUser(any(User.class));
    }

    @Test
    void getAllUsers_whenCalled_thenReturnsListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDto.getId()));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void updateUser_whenValidUser_thenReturnsUpdatedUser() throws Exception {
        when(userMapper.toUser(any(UserDto.class))).thenReturn(user);
        when(userService.updateUser(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()));

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void deleteUser_whenCalled_thenReturns204() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }
}