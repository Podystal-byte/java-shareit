package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exception.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void handleMethodArgumentNotValidException_whenExceptionIsThrown_thenReturnsCorrectResponse() {
        String expectedMessage = "default message";
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", expectedMessage);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        Map<String, String> response = handlerError.handleMethodArgumentNotValidException(exception);

        assertNotNull(response);
        assertEquals(expectedMessage, response.get("error"));
    }
}
