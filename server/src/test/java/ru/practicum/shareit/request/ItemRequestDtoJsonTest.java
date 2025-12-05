package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private JacksonTester<ItemRequestDto> json;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void serializeItemRequestDtoReturnsValidJson() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 10, 0);
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Need a power drill for home repairs", 2L, created);

        JsonContent<ItemRequestDto> jsonContent = json.write(requestDto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a power drill for home repairs");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestorId").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo("2024-01-01T10:00:00");
    }

    @Test
    void deserializeJsonWithDateReturnsItemRequestDto() throws Exception {
        String jsonContent = "{\"id\":1,\"description\":\"Need hammer\",\"requestorId\":2,\"created\":\"2024-01-01T10:00:00\"}";

        ItemRequestDto requestDto = json.parse(jsonContent).getObject();

        assertThat(requestDto.getId()).isEqualTo(1L);
        assertThat(requestDto.getDescription()).isEqualTo("Need hammer");
        assertThat(requestDto.getRequestorId()).isEqualTo(2L);
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }

    @Test
    void deserializeJsonWithoutRequestorIdReturnsItemRequestDtoWithNullRequestorId() throws Exception {
        String jsonContent = "{\"description\":\"Need tool\"}";

        ItemRequestDto requestDto = json.parse(jsonContent).getObject();

        assertThat(requestDto.getRequestorId()).isNull();
        assertThat(requestDto.getDescription()).isEqualTo("Need tool");
        assertThat(requestDto.getId()).isNull();
        assertThat(requestDto.getCreated()).isNull();
    }

    @Test
    void serializeItemRequestDtoWithoutIdReturnsJsonWithoutId() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(null, "Need screwdriver", 2L, LocalDateTime.now());

        JsonContent<ItemRequestDto> jsonContent = json.write(requestDto);

        assertThat(jsonContent).doesNotHaveJsonPathValue("$.id");
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Need screwdriver");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.requestorId").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathStringValue("$.created").isNotNull();
    }
}