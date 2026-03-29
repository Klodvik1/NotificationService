package io.github.Klodvik1.kafka;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserNotificationKafkaListenerIntegrationTest {
    @Container
    static final KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Value("${app.kafka.topics.user-notification}")
    private String userNotificationTopic;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
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
    void kafkaListener_shouldSendCreatedEmail() throws Exception {
        String email = "kafka-created@example.com";
        String payload = """
                {
                  "operation": "USER_CREATED",
                  "email": "%s"
                }
                """.formatted(email);

        sendKafkaMessage(payload, email);

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Уведомление о создании аккаунта", messages[0].getSubject());
        assertEquals(email, messages[0].getAllRecipients()[0].toString());
        assertEquals(
                "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                messages[0].getContent().toString().trim()
        );
    }

    @Test
    void kafkaListener_shouldSendDeletedEmail() throws Exception {
        String email = "kafka-deleted@example.com";
        String payload = """
                {
                  "operation": "USER_DELETED",
                  "email": "%s"
                }
                """.formatted(email);

        sendKafkaMessage(payload, email);

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Уведомление об удалении аккаунта", messages[0].getSubject());
        assertEquals(email, messages[0].getAllRecipients()[0].toString());
        assertEquals(
                "Здравствуйте! Ваш аккаунт был удалён.",
                messages[0].getContent().toString().trim()
        );
    }

    private void sendKafkaMessage(String payload, String key) throws Exception {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            producer.send(new ProducerRecord<>(userNotificationTopic, key, payload))
                    .get(10, TimeUnit.SECONDS);
        }
    }
}
