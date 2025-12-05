package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Test
    void createValidItemReturnsItemDto() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Drill", "Heavy duty drill", true, null);
        ItemDto outputDto = new ItemDto(1L, "Drill", "Heavy duty drill", true, null);

        when(itemService.create(any(ItemDto.class), anyLong())).thenReturn(outputDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(inputDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Heavy duty drill")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void updateValidUpdateReturnsUpdatedItem() throws Exception {
        ItemDto updateDto = new ItemDto(1L, "Updated Drill", "Updated description", false, null);
        when(itemService.update(any(ItemDto.class), anyLong())).thenReturn(updateDto);

        mvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(updateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Drill")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void getByIdExistingItemReturnsItem() throws Exception {
        ItemWithBookingsDto itemDto = new ItemWithBookingsDto(1L, "Drill", "Description", true, null,
                null, null, Collections.emptyList());
        when(itemService.getById(anyLong(), anyLong())).thenReturn(itemDto);

        mvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")));
    }

    @Test
    void getByOwnerReturnsItemsList() throws Exception {
        List<ItemWithBookingsDto> items = List.of(
                new ItemWithBookingsDto(1L, "Item 1", "Desc 1", true, null, null, null, Collections.emptyList()),
                new ItemWithBookingsDto(2L, "Item 2", "Desc 2", true, null, null, null, Collections.emptyList())
        );
        when(itemService.getByOwnerId(anyLong())).thenReturn(items);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Item 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Item 2")));
    }

    @Test
    void searchValidTextReturnsItems() throws Exception {
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Drill", "Heavy duty", true, null));
        when(itemService.search(anyString())).thenReturn(items);

        mvc.perform(get("/items/search")
                        .param("text", "drill")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));
    }

    @Test
    void deleteValidIdReturnsOk() throws Exception {
        mvc.perform(delete("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentValidCommentReturnsCommentResponse() throws Exception {
        CommentDto input = new CommentDto("Great item!");
        CommentResponseDto output = new CommentResponseDto(1L, "Great item!", "John", LocalDateTime.now());

        when(itemService.addComment(anyLong(), any(CommentDto.class), anyLong())).thenReturn(output);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great item!")))
                .andExpect(jsonPath("$.authorName", is("John")))
                .andExpect(jsonPath("$.created", notNullValue()));
    }
}