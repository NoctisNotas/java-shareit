package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.service.BookingService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Test
    void bookingServiceIsImplemented() {
        assertNotNull(bookingService);
    }
}