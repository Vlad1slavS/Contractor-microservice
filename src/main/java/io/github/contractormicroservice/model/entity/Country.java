package io.github.contractormicroservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

/**
 * Класс модели страны
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Country implements Persistable<String> {

    @Id
    private String id;

    private String name;

    @Builder.Default
    private boolean isActive = true;

    @Transient
    @Builder.Default
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markAsNew() {
        this.isNew = true;
    }

    public void markAsExisting() {
        this.isNew = false;
    }

}



