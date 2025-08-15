package io.github.contractormicroservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Сущность Outbox
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("outbox_events")
public class OutboxEvent implements Persistable<Long> {

    @Id
    private Long id;

    private String aggregateId;

    private String aggregateType;

    private String eventType;

    private String payload;

    private String routingKey;

    @Column("exchange_name")
    private String exchange;

    @Builder.Default
    private Boolean processed = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime processedAt;

    @Builder.Default
    @JsonIgnore
    @Transient
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markAsNew() {
        this.isNew = true;
    }

}
