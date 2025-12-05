package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeUserDtoReturnsValidJson() throws Exception {
        UserDto userDto = new UserDto(1L, "John Doe", "john@example.com");
        String json = objectMapper.writeValueAsString(userDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"John Doe\"");
        assertThat(json).contains("\"email\":\"john@example.com\"");
    }

    @Test
    void deserializeJsonReturnsUserDto() throws Exception {
        String json = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("John Doe");
        assertThat(userDto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void deserializeJsonWithoutIdReturnsUserDtoWithNullId() throws Exception {
        String json = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertThat(userDto.getId()).isNull();
        assertThat(userDto.getName()).isEqualTo("John Doe");
        assertThat(userDto.getEmail()).isEqualTo("john@example.com");
    }
}