package io.github.Klodvik1.service;

import io.github.Klodvik1.event.UserOperationType;
import org.springframework.stereotype.Service;

@Service
public class NotificationMessageService {

    public String getSubject(UserOperationType operation) {
        return switch (operation) {
            case USER_CREATED -> "Уведомление о создании аккаунта";
            case USER_DELETED -> "Уведомление об удалении аккаунта";
        };
    }

    public String getText(UserOperationType operation) {
        return switch (operation) {
            case USER_CREATED -> "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
            case USER_DELETED -> "Здравствуйте! Ваш аккаунт был удалён.";
        };
    }
}
