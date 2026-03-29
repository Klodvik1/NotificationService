package io.github.Klodvik1.dto;

import io.github.Klodvik1.event.UserOperationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequestDto(
        @NotNull(message = "Операция обязательна.")
        UserOperationType operation,

        @NotBlank(message = "Email обязателен.")
        @Email(message = "Некорректный формат email.")
        String email) {
}
