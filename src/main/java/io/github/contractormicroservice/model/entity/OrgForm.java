package io.github.contractormicroservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Класс модели организационной формы
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgForm {

    @Id
    private Long id;

    private String name;

    @Builder.Default
    private boolean isActive = true;

}

