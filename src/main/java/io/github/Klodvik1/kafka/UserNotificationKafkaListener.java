package io.github.Klodvik1.kafka;

import io.github.Klodvik1.event.UserNotificationEvent;
import io.github.Klodvik1.service.NotificationFacade;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationKafkaListener {
    private final NotificationFacade notificationFacade;

    public UserNotificationKafkaListener(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @KafkaListener(topics = "${app.kafka.topics.user-notification}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserNotificationEvent(UserNotificationEvent event) {
        notificationFacade.processUserEvent(event);
    }
}
