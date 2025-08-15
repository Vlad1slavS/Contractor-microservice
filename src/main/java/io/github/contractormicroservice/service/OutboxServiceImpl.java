package io.github.contractormicroservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.model.entity.OutboxEvent;
import io.github.contractormicroservice.repository.outbox.OutboxEventRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Сервис работы с Outbox
 */
@Service
@EnableScheduling
public class OutboxServiceImpl implements OutboxService {

    private static final int rabbitReceiveTimeout = 5;
    private static final int scheduledDelay = 5000;
    private final Logger log = LogManager.getLogger(OutboxServiceImpl.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OutboxServiceImpl(OutboxEventRepository outboxEventRepository, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional("transactionManager")
    @Override
    public void saveOutboxEvent(String aggregateId, String aggregateType, String eventType,
                                Object payload, String exchange, String routingKey) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent event = OutboxEvent.builder()
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .exchange(exchange)
                    .routingKey(routingKey)
                    .build();

            event.markAsNew();

            outboxEventRepository.save(event);

            log.debug("Outbox event saved: aggregateId={}, eventType={}", aggregateId, eventType);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payload for outbox event", e);
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }

    /**
     * Периодически смотрит невыполненные (неотправленные) сообщения и отправляет их в Rabbit
     * Отправка в CONTRACTORS_CONTRACTOR_EXCHANGE с проверкой доставки
     */
    @Scheduled(fixedDelay = scheduledDelay)
    @Transactional("transactionManager")
    @Override
    public void publishOutboxEvents() {
        List<OutboxEvent> unprocessedEvents = outboxEventRepository.findUnprocessedEvents();

        for (OutboxEvent event : unprocessedEvents) {
            try {
                String correlationId = "outbox-" + event.getId();
                CorrelationData correlationData = new CorrelationData(correlationId);

                rabbitTemplate.convertAndSend(
                        event.getExchange(),
                        event.getRoutingKey(),
                        event.getPayload(),
                        correlationData
                );

                CorrelationData.Confirm confirm = correlationData.getFuture().get(rabbitReceiveTimeout, TimeUnit.SECONDS);

                if (confirm.isAck()) {
                    outboxEventRepository.markAsProcessed(event.getId());
                    log.debug("Event published and confirmed: id={}", event.getId());
                } else {
                    log.error("Event rejected by broker: id={}, reason={}",
                            event.getId(), confirm.getReason());
                }

            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to get confirmation for event: id={}", event.getId(), e);
            } catch (TimeoutException e) {
                log.error("Confirmation timeout for event: id={}", event.getId(), e);
            } catch (Exception e) {
                log.error("Unexpected error publishing event: id={}", event.getId(), e);
            }
        }

    }

}
