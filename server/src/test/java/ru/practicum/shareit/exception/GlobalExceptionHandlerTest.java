package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFoundException_ShouldReturnNotFoundStatus() {
        NotFoundException exception = new NotFoundException("Not found message");

        Map<String, String> response = exceptionHandler.handleNotFoundException(exception);

        assertNotNull(response);
        assertEquals("Not found message", response.get("error"));
    }

    @Test
    void handleValidationException_ShouldReturnConflictStatus() {
        ValidationException exception = new ValidationException("Validation message");

        Map<String, String> response = exceptionHandler.handleValidationException(exception);

        assertNotNull(response);
        assertEquals("Validation message", response.get("error"));
    }

    @Test
    void handleBadRequestException_ShouldReturnBadRequestStatus() {
        BadRequestException exception = new BadRequestException("Bad request message");

        Map<String, String> response = exceptionHandler.handleBadRequestException(exception);

        assertNotNull(response);
        assertEquals("Bad request message", response.get("error"));
    }

    @Test
    void handleMissingRequestHeaderException_ShouldReturnBadRequest() {
        MissingRequestHeaderException exception =
                new MissingRequestHeaderException("X-Sharer-User-Id", null);

        Map<String, String> response = exceptionHandler.handleMissingHeaders(exception);

        assertNotNull(response);
        assertTrue(response.get("error").contains("X-Sharer-User-Id"));
    }

    @Test
    void handleOtherExceptions_ShouldReturnInternalServerError() {
        Exception exception = new RuntimeException("Some unexpected error");

        Map<String, String> response = exceptionHandler.handleOtherExceptions(exception);

        assertNotNull(response);
        assertTrue(response.get("error").contains("Internal server error"));
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "default message");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        Map<String, String> response = exceptionHandler.handleValidationExceptions(exception);

        assertNotNull(response);
        assertNotNull(response.get("error"));
        assertTrue(response.get("error").contains("field: default message"));
    }

    @Test
    void handleMethodArgumentNotValidException_WithMultipleErrors_ReturnsFirstErrorMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("object", "field1", "error 1");
        FieldError error2 = new FieldError("object", "field2", "error 2");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));
        when(exception.getBindingResult()).thenReturn(bindingResult);

        Map<String, String> response = exceptionHandler.handleValidationExceptions(exception);

        assertNotNull(response);
        assertTrue(response.get("error").contains("field1: error 1"));
    }

    @Test
    void handleMethodArgumentNotValidException_WithNoFieldErrors_ReturnsGenericMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        when(exception.getBindingResult()).thenReturn(bindingResult);

        Map<String, String> response = exceptionHandler.handleValidationExceptions(exception);

        assertNotNull(response);
        assertEquals("Validation error", response.get("error"));
    }
}