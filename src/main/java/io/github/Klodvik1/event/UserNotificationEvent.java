package io.github.Klodvik1.event;

public record UserNotificationEvent(
        UserOperationType operation,
        String email) {
}
