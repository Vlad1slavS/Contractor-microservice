package io.github.contractormicroservice.model.dto;

import io.github.contractormicroservice.model.entity.OrgForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object для организационной формы (для ответа пользователю)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "OrgFormDTO",
        description = "Объект передачи данных для организационной формы",
        example = """
        {
            "id": 1,
            "name": "ООО"
        }
        """
)
public class OrgFormDTO {

    @Schema(
            description = "Уникальный идентификатор организационной формы",
            example = "1",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
            description = "Название организационной формы",
            example = "ООО ...",
            minLength = 1,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Название организационной формы не может быть пустым")
    @NotNull(message = "Название организационной формы не может быть null")
    private String name;

    public static OrgFormDTO fromEntity(OrgForm orgForm) {
        if (orgForm == null) {
            return null;
        }

        return OrgFormDTO.builder()
                .id(orgForm.getId())
                .name(orgForm.getName())
                .build();
    }

    public static List<OrgFormDTO> fromEntityList(List<OrgForm> orgForms) {
        if (orgForms == null) {
            return null;
        }

        return orgForms.stream()
                .map(OrgFormDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
