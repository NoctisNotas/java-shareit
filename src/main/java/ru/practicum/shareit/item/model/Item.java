package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Getter
@Setter
public class Item {

    private Long id;

    @NotBlank(message = "Необходимо указать название вещи")
    private String name;

    private String description;

    @NotNull(message = "Необходимо указать доступна ли вещь")
    private Boolean isAvailable;

    private User owner;

    private Long itemRequestId;
}
