package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .build()
        );
    }

    public ResponseEntity<Object> getAll() {
        return get("", null, null);
    }

    public ResponseEntity<Object> getById(Long id) {
        return get("/" + id, null, null);
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post("", null, userDto);
    }

    public ResponseEntity<Object> update(Long id, UserDto userDto) {
        return patch("/" + id, null, null, userDto);
    }

    public ResponseEntity<Object> delete(Long id) {
        return delete("/" + id, null);
    }
}