package io.github.Klodvik1.controller;

import io.github.Klodvik1.dto.NotificationRequestDto;
import io.github.Klodvik1.dto.NotificationResponseDto;
import io.github.Klodvik1.event.UserNotificationEvent;
import io.github.Klodvik1.service.NotificationFacade;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationFacade notificationFacade;

    public NotificationController(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @PostMapping("/user-event")
    public ResponseEntity<NotificationResponseDto> sendNotification(
            @Valid @RequestBody NotificationRequestDto requestDto) {
        UserNotificationEvent event = new UserNotificationEvent(
                requestDto.operation(),
                requestDto.email()
        );

        notificationFacade.processUserEvent(event);

        return ResponseEntity.ok(new NotificationResponseDto("SENT"));
    }
}
