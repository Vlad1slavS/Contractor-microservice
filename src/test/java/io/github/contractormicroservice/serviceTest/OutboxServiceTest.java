package io.github.contractormicroservice.serviceTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.model.entity.OutboxEvent;
import io.github.contractormicroservice.repository.outbox.OutboxEventRepository;
import io.github.contractormicroservice.service.OutboxServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxServiceImpl outboxService;

    private TestPayload testPayload;

    @BeforeEach
    void setUp() {
        testPayload = new TestPayload("test-name", "test-value");
    }

    @Test
    void saveOutboxEvent_shouldSaveSuccessfully() throws JsonProcessingException {

        String payloadJson = "{\"name\":\"test-name\",\"value\":\"test-value\"}";

        when(objectMapper.writeValueAsString(testPayload)).thenReturn(payloadJson);

        outboxService.saveOutboxEvent("contractor-123", "Contractor", "ContractorUpdated",
                testPayload, "exchange", "routing.key");

        verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));

    }

    @Test
    void saveOutboxEvent_shouldThrowException() throws JsonProcessingException {

        when(objectMapper.writeValueAsString(testPayload))
                .thenThrow(new JsonProcessingException("Error") {});

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            outboxService.saveOutboxEvent("contractor-123", "Contractor", "ContractorUpdated",
                    testPayload, "exchange", "routing.key");
        });

        assertEquals("Failed to serialize payload", exception.getMessage());
    }

    @Test
    void publishOutboxEvents_shouldPublish() {

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId("test-123")
                .eventType("TestEvent")
                .aggregateType("Test")
                .payload("test-payload")
                .exchange("test-exchange")
                .routingKey("test.routing")
                .processed(false)
                .createdAt(LocalDateTime.now())
                .build();


        List<OutboxEvent> events = new ArrayList<>();
        events.add(outboxEvent);
        when(outboxEventRepository.findUnprocessedEvents()).thenReturn(events);

        outboxService.publishOutboxEvents();

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString(), any(), any(CorrelationData.class));
    }

    @Test
    void publishOutboxEvents_shouldNotPublish() {

        when(outboxEventRepository.findUnprocessedEvents()).thenReturn(new ArrayList<>());

        outboxService.publishOutboxEvents();

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString(), any(CorrelationData.class));

    }


    private record TestPayload(String name, String value) {}
}