package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeItemDtoReturnsValidJson() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Drill", "Heavy duty drill", true, 10L);
        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Drill\"");
        assertThat(json).contains("\"description\":\"Heavy duty drill\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":10");
    }

    @Test
    void deserializeJsonReturnsItemDto() throws Exception {
        String json = "{\"id\":1,\"name\":\"Drill\",\"description\":\"Heavy duty drill\",\"available\":true,\"requestId\":10}";
        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Drill");
        assertThat(itemDto.getDescription()).isEqualTo("Heavy duty drill");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(10L);
    }

    @Test
    void deserializeJsonWithoutIdReturnsItemDtoWithNullId() throws Exception {
        String json = "{\"name\":\"Drill\",\"description\":\"Heavy duty drill\",\"available\":true}";
        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getId()).isNull();
        assertThat(itemDto.getName()).isEqualTo("Drill");
        assertThat(itemDto.getDescription()).isEqualTo("Heavy duty drill");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isNull();
    }

    @Test
    void deserializeJsonWithNullAvailableReturnsItemDtoWithNullAvailable() throws Exception {
        String json = "{\"name\":\"Drill\",\"description\":\"Heavy duty drill\"}";
        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getAvailable()).isNull();
    }
}