package io.github.Klodvik1.service;

import io.github.Klodvik1.event.UserNotificationEvent;
import org.springframework.stereotype.Service;

@Service
public class NotificationFacade {
    private final NotificationMessageService notificationMessageService;
    private final EmailService emailService;

    public NotificationFacade(
            NotificationMessageService notificationMessageService,
            EmailService emailService) {
        this.notificationMessageService = notificationMessageService;
        this.emailService = emailService;
    }

    public void processUserEvent(UserNotificationEvent event) {
        String subject = notificationMessageService.getSubject(event.operation());
        String text = notificationMessageService.getText(event.operation());

        emailService.sendEmail(event.email(), subject, text);
    }
}
