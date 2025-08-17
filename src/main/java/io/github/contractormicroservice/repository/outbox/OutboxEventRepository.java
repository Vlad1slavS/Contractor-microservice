package io.github.contractormicroservice.repository.outbox;

import io.github.contractormicroservice.model.entity.OutboxEvent;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с таблицей Outbox
 */
@Repository
public interface OutboxEventRepository extends CrudRepository<OutboxEvent, Long> {

    @Query("SELECT * FROM outbox_events WHERE processed = false ORDER BY created_at ASC LIMIT 100")
    List<OutboxEvent> findUnprocessedEvents();

    @Modifying
    @Query("UPDATE outbox_events SET processed = true, processed_at = CURRENT_TIMESTAMP WHERE id = :id")
    void markAsProcessed(String id);

}
