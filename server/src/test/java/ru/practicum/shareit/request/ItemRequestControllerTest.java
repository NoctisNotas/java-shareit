package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

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

@WebMvcTest(controllers = ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestControllerTest {

    private final MockMvc mvc;
    private final ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createValidRequestReturnsRequestDto() throws Exception {
        ItemRequestDto input = new ItemRequestDto(null, "Need a drill", null, null);
        ItemRequestDto output = new ItemRequestDto(1L, "Need a drill", 2L, LocalDateTime.now());

        when(requestService.create(any(ItemRequestDto.class), anyLong())).thenReturn(output);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 2L)
                        .content(mapper.writeValueAsString(input))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a drill")))
                .andExpect(jsonPath("$.requestorId", is(2)))
                .andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    void getUserRequestsReturnsRequestsList() throws Exception {
        List<ItemRequestResponseDto> requests = List.of(
                new ItemRequestResponseDto(1L, "Need drill", LocalDateTime.now(), Collections.emptyList()),
                new ItemRequestResponseDto(2L, "Need hammer", LocalDateTime.now().minusDays(1), Collections.emptyList())
        );

        when(requestService.getUserRequests(anyLong())).thenReturn(requests);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Need drill")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].description", is("Need hammer")));
    }

    @Test
    void getAllRequestsReturnsAllRequests() throws Exception {
        List<ItemRequestResponseDto> requests = List.of(
                new ItemRequestResponseDto(1L, "Request 1", LocalDateTime.now(), Collections.emptyList()));

        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(requests);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Request 1")));
    }

    @Test
    void getAllRequestsDefaultParamsReturnsRequests() throws Exception {
        List<ItemRequestResponseDto> requests = List.of();

        when(requestService.getAllRequests(anyLong(), anyInt(), anyInt())).thenReturn(requests);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void getRequestByIdExistingRequestReturnsRequest() throws Exception {
        ItemRequestResponseDto request = new ItemRequestResponseDto(1L, "Need tool",
                LocalDateTime.now(), Collections.emptyList());

        when(requestService.getRequestById(anyLong(), anyLong())).thenReturn(request);

        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need tool")));
    }

    @Test
    void getNonExistentRequestShouldReturnNotFound() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong())).thenThrow(new NotFoundException("Запрос не найден"));

        mvc.perform(get("/requests/999999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}