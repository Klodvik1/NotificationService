package io.github.Klodvik1.controller;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.kafka.listener.auto-startup=false"
        }
)
@AutoConfigureMockMvc
class NotificationControllerIntegrationTest {
    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerMailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> ServerSetupTest.SMTP.getPort());
    }

    @BeforeEach
    void setUp() {
        try {
            greenMail.purgeEmailFromAllMailboxes();
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Test
    void sendNotification_shouldSendCreatedEmail() throws Exception {
        String requestBody = """
                {
                  "operation": "USER_CREATED",
                  "email": "created@example.com"
                }
                """;

        mockMvc.perform(post("/api/notifications/user-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Уведомление о создании аккаунта", messages[0].getSubject());
        assertEquals("created@example.com", messages[0].getAllRecipients()[0].toString());
        assertEquals(
                "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                messages[0].getContent().toString().trim()
        );
    }

    @Test
    void sendNotification_shouldSendDeletedEmail() throws Exception {
        String requestBody = """
                {
                  "operation": "USER_DELETED",
                  "email": "deleted@example.com"
                }
                """;

        mockMvc.perform(post("/api/notifications/user-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Уведомление об удалении аккаунта", messages[0].getSubject());
        assertEquals("deleted@example.com", messages[0].getAllRecipients()[0].toString());
        assertEquals(
                "Здравствуйте! Ваш аккаунт был удалён.",
                messages[0].getContent().toString().trim()
        );
    }
}
