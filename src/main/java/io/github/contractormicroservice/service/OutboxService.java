package io.github.contractormicroservice.service;

public interface OutboxService {

    /**
     * Метод для сохранения Outbox сообщения в БД
     */
    void saveOutboxEvent(String aggregateId, String aggregateType, String eventType,
                                Object payload, String exchange, String routingKey);

    /**
     * Метод для отправки сообщений в Rabbit
     */
    void publishOutboxEvents();

}
