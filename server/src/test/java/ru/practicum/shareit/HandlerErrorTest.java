package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HandlerErrorTest {

    private HandlerError handlerError;

    @BeforeEach
    public void setUp() {
        handlerError = new HandlerError();
    }

    @Test
    public void handleNotFoundException_whenExceptionIsThrown_thenReturnsCorrectResponse() {
        NotFoundException exception = new NotFoundException("Not found message");

        Map<String, String> response = handlerError.handle(exception);

        assertNotNull(response);
        assertEquals("Not found: Not found message", response.get("error"));
    }

    @Test
    public void handleValidationException_whenExceptionIsThrown_thenReturnsCorrectResponse() {
        ValidationException exception = new ValidationException("Validation failed");

        Map<String, String> response = handlerError.handleException(exception);

        assertNotNull(response);
        assertEquals("Validation failed", response.get("error"));
    }

    @Test
    public void handleConflictException_whenExceptionIsThrown_thenReturnsCorrectResponse() {
        ConflictException exception = new ConflictException("Conflict occurred");

        Map<String, String> response = handlerError.handleConflictException(exception);

        assertNotNull(response);
        assertEquals("Conflict occurred", response.get("error"));
    }

    @Test
    public void handleUserException_whenExceptionIsThrown_thenReturnsCorrectResponse() {
        UserException exception = new UserException("User error");

        Map<String, String> response = handlerError.handleForbiddenException(exception);

        assertNotNull(response);
        assertEquals("User error", response.get("error"));
    }
}
