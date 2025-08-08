package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable int id) {
        return UserDtoMapper.map(userService.getUserById(id));
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserDto userDto) throws ConflictException, ValidationException {
        User user = UserDtoMapper.mapToUser(userDto);
        User createdUser = userService.addUser(user);
        return UserDtoMapper.map(createdUser);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserDtoMapper::map)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable int id, @Valid @RequestBody UserDto userDto) throws ConflictException {
        User updatedUser = UserDtoMapper.mapToUser(userDto);
        updatedUser.setId(id);
        User result = userService.updateUser(updatedUser);
        return UserDtoMapper.map(result);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }
}