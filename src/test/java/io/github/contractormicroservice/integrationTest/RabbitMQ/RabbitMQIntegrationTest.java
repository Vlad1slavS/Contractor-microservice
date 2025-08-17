package io.github.contractormicroservice.integrationTest.RabbitMQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.model.dto.ContractorDTO;
import io.github.contractormicroservice.model.entity.OutboxEvent;
import io.github.contractormicroservice.repository.outbox.OutboxEventRepository;
import io.github.contractormicroservice.service.OutboxService;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@Import(TestConfig.class)
class RabbitMQIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void configureRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
    }

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageReceiver messageReceiver;

    private static final String EXCHANGE = "test_exchange";
    private static final String QUEUE = "test_queue";
    private static final String ROUTING_KEY = "test.key";

    @BeforeEach
    void setup() {
        outboxRepository.deleteAll();
        rabbitAdmin.purgeQueue(QUEUE, false);
        messageReceiver.clear();
    }

    @Test
    void sendMessage_shouldSendMessageThroughOutbox() throws Exception {

        ContractorDTO contractor = new ContractorDTO();
        contractor.setId("TEST-1");
        contractor.setName("Test Company");
        contractor.setCountry("RU");

        outboxService.saveOutboxEvent(
                contractor.getId(),
                "Contractor",
                "ContractorCreated",
                contractor,
                EXCHANGE,
                ROUTING_KEY
        );

        List<OutboxEvent> events = outboxRepository.findUnprocessedEvents();
        assertEquals(1, events.size());

        outboxService.publishOutboxEvents();

        await().atMost(Duration.ofSeconds(5))
                .until(() -> messageReceiver.getLastMessage() != null);

        String message = messageReceiver.getLastMessage();
        ContractorDTO received = objectMapper.readValue(message, ContractorDTO.class);

        assertEquals("TEST-1", received.getId());
        assertEquals("Test Company", received.getName());
        assertEquals("RU", received.getCountry());

        List<OutboxEvent> remaining = outboxRepository.findUnprocessedEvents();
        assertEquals(0, remaining.size());
    }

    @Test
    void sendMessages_shouldHandleMultipleMessages() {

        ContractorDTO contractor1 = new ContractorDTO();
        contractor1.setId("TEST-1");
        contractor1.setName("Company 1");

        ContractorDTO contractor2 = new ContractorDTO();
        contractor2.setId("TEST-2");
        contractor2.setName("Company 2");

        outboxService.saveOutboxEvent(
                contractor1.getId(), "Contractor", "Created", contractor1,
                EXCHANGE, ROUTING_KEY
        );
        outboxService.saveOutboxEvent(
                contractor2.getId(), "Contractor", "Created", contractor2,
                EXCHANGE, ROUTING_KEY
        );

        outboxService.publishOutboxEvents();

        await().atMost(Duration.ofSeconds(5))
                .until(() -> messageReceiver.getMessageCount() == 2);

        assertEquals(2, messageReceiver.getMessageCount());
    }

    @Test
    void sendMessageToNotExistingExchange_shouldDontPublicWhenPublishingFails() {

        ContractorDTO contractor = new ContractorDTO();
        contractor.setId("TEST");
        contractor.setName("Test company");

        outboxService.saveOutboxEvent(
                contractor.getId(),
                "Contractor",
                "CREATED",
                contractor,
                "non_existing_exchange",
                "test.key"
        );

        outboxService.publishOutboxEvents();

        List<OutboxEvent> events = outboxRepository.findUnprocessedEvents();
        assertEquals(1, events.size());
        assertEquals("TEST", events.getFirst().getAggregateId());

    }


    @Getter
    public static class MessageReceiver {
        private String lastMessage;
        private int messageCount = 0;

        @RabbitListener(queues = "test_queue")
        public void receive(String message) {
            this.lastMessage = message;
            this.messageCount++;
        }

        public void clear() {
            lastMessage = null;
            messageCount = 0;
        }
    }
}