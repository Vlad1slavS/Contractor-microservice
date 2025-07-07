package io.github.contractormicroservice.model.dto;

import io.github.contractormicroservice.model.entity.Industry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object для индустриального кода (для ответа пользователю)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "IndustryDTO",
        description = "Объект передачи данных для индустриального кода",
        example = """
        {
            "id": 1,
            "name": "Строительство"
        }
        """
)
public class IndustryDTO {

    @Schema(
            description = "Уникальный идентификатор индустриального кода",
            example = "1",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "id индустриального кода не может быть пустым")
    private Long id;

    @Schema(
            description = "Название индустриального кода",
            example = "Строительство",
            minLength = 1,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Название индустриального кода не может быть пустым")
    @NotNull(message = "Название индустриального кода не может быть null")
    private String name;

    public static IndustryDTO fromEntity(Industry industry) {
        if (industry == null) {
            return null;
        }

        return IndustryDTO.builder()
                .id(industry.getId())
                .name(industry.getName())
                .build();
    }

    public static List<IndustryDTO> fromEntityList(List<Industry> industries) {
        if (industries == null) {
            return null;
        }

        return industries.stream()
                .map(IndustryDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
