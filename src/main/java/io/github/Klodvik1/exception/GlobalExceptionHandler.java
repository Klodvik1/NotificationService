package io.github.Klodvik1.exception;

import io.github.Klodvik1.dto.NotificationResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<NotificationResponseDto> handleValidationException(
            MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                .body(new NotificationResponseDto("VALIDATION_ERROR"));
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<NotificationResponseDto> handleMailException(MailException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new NotificationResponseDto("MAIL_SEND_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<NotificationResponseDto> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new NotificationResponseDto("INTERNAL_ERROR"));
    }
}
