package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private Long Id;

    private String name;

    @NotBlank(message = "Адрес электронной почты не должен быть пустым")
    @Email(message = "Адрес электронной почты должен соответствовать формату")
    private String email;
}
