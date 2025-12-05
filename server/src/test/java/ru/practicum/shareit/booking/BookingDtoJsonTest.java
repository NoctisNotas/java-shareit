package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private JacksonTester<BookingDto> json;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void serializeBookingDtoReturnsValidJson() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0);
        BookingDto bookingDto = new BookingDto(1L, start, end, 10L, "WAITING");

        JsonContent<BookingDto> jsonContent = json.write(bookingDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo("2024-01-01T10:00:00");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo("2024-01-02T10:00:00");
    }

    @Test
    void serializeBookingDtoWithoutIdReturnsJsonWithoutId() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 10, 0);
        BookingDto bookingDto = new BookingDto(null, start, end, 10L, "WAITING");

        JsonContent<BookingDto> jsonContent = json.write(bookingDto);

        assertThat(jsonContent).doesNotHaveJsonPathValue("$.id");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start")
                .isEqualTo("2024-01-01T10:00:00");
        assertThat(jsonContent).extractingJsonPathStringValue("$.end")
                .isEqualTo("2024-01-02T10:00:00");
    }

    @Test
    void deserializeJsonWithDatesReturnsBookingDto() throws Exception {
        String jsonContent = "{\"id\":1,\"start\":\"2024-01-01T10:00:00\"," +
                "\"end\":\"2024-01-02T10:00:00\",\"itemId\":10,\"status\":\"WAITING\"}";
        BookingDto bookingDto = json.parse(jsonContent).getObject();

        assertThat(bookingDto.getId()).isEqualTo(1L);
        assertThat(bookingDto.getItemId()).isEqualTo(10L);
        assertThat(bookingDto.getStatus()).isEqualTo("WAITING");
        assertThat(bookingDto.getStart())
                .isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(bookingDto.getEnd())
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));
    }

    @Test
    void deserializeJsonWithoutStatusReturnsBookingDtoWithNullStatus() throws Exception {
        String jsonContent = "{\"start\":\"2024-01-01T10:00:00\",\"end\":\"2024-01-02T10:00:00\",\"itemId\":10}";
        BookingDto bookingDto = json.parse(jsonContent).getObject();

        assertThat(bookingDto.getStatus()).isNull();
        assertThat(bookingDto.getStart())
                .isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(bookingDto.getEnd())
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));
        assertThat(bookingDto.getItemId()).isEqualTo(10L);
    }

    @Test
    void toBookingShouldMapDtoToEntity() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto dto = new BookingDto(1L, start, end, 10L, "WAITING");

        Booking booking = BookingMapper.toBooking(dto);

        assertNotNull(booking);
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
    }
}