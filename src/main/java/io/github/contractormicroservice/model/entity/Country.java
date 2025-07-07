package io.github.contractormicroservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Класс модели страны
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Country {

    @Id
    private String id;

    private String name;

    @Builder.Default
    private boolean isActive = true;

}


