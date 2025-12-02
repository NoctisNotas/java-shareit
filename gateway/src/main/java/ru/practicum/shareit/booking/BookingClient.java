package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .build()
        );
    }

    public ResponseEntity<Object> create(BookingDto bookingDto, Long userId) {
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> updateStatus(Long bookingId, Boolean approved, Long userId) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId, userId, parameters, null);
    }

    public ResponseEntity<Object> getById(Long bookingId, Long userId) {
        return get("/" + bookingId, userId, null);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, String state, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, String state, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get("/owner", userId, parameters);
    }
}