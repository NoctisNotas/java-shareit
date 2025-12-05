package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createValidBookingReturnsBookingResponse() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto input = new BookingDto(null, start, end, 1L, "WAITING");
        BookingResponseDto output = new BookingResponseDto(1L, start, end, BookingStatus.WAITING,
                new UserDto(2L, "Booker", "booker@example.com"),
                new ItemDto(1L, "Drill", "Description", true, null));

        when(bookingService.create(any(BookingDto.class), anyLong())).thenReturn(output);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is(2)))
                .andExpect(jsonPath("$.item.id", is(1)));
    }

    @Test
    void updateStatusApproveReturnsUpdatedBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingResponseDto output = new BookingResponseDto(1L, start, end, BookingStatus.APPROVED,
                new UserDto(2L, "Booker", "booker@example.com"),
                new ItemDto(1L, "Drill", "Description", true, null));

        when(bookingService.updateStatus(anyLong(), anyBoolean(), anyLong())).thenReturn(output);

        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void getByIdExistingBookingReturnsBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingResponseDto output = new BookingResponseDto(1L, start, end, BookingStatus.WAITING,
                new UserDto(2L, "Booker", "booker@example.com"),
                new ItemDto(1L, "Drill", "Description", true, null));

        when(bookingService.getById(anyLong(), anyLong())).thenReturn(output);

        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is(2)));
    }

    @Test
    void getUserBookingsAllStateReturnsBookings() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        List<BookingResponseDto> bookings = List.of(
                new BookingResponseDto(1L, start, end, BookingStatus.APPROVED,
                        new UserDto(2L, "Booker", "booker@example.com"),
                        new ItemDto(1L, "Drill", "Description", true, null))
        );

        when(bookingService.getUserBookings(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("APPROVED")));
    }

    @Test
    void getOwnerBookingsAllStateReturnsBookings() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        List<BookingResponseDto> bookings = List.of(
                new BookingResponseDto(1L, start, end, BookingStatus.WAITING,
                        new UserDto(2L, "Booker", "booker@example.com"),
                        new ItemDto(1L, "Drill", "Description", true, null))
        );

        when(bookingService.getOwnerBookings(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")));
    }

    @Test
    void getUserBookingsDefaultParamsReturnsBookings() throws Exception {
        List<BookingResponseDto> bookings = List.of();

        when(bookingService.getUserBookings(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }
}