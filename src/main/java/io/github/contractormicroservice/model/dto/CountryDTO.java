package io.github.contractormicroservice.model.dto;

import io.github.contractormicroservice.model.entity.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object для страны (для ответа пользователю)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "CountryDTO",
        description = "Объект передачи данных для страны",
        example = """
            {
                "id": "RU",
                    "name": "Россия"
            }
            """
)
public class CountryDTO {

    @Schema(
            description = "Уникальный идентификатор страны",
            example = "RU",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "ID страны не может быть пустым")
    @NotNull(message = "ID страны не может быть null")
    @Size(min = 1, max = 5, message = "ID страны должен содержать от 1 до 5 букв")
    private String id;

    @Schema(
            description = "Название страны",
            example = "Россия",
            minLength = 1,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Название страны не может быть пустым")
    @NotNull(message = "Название страны не может быть null")
    private String name;

    /**
     * Преобразование сущности в DTO
     * @param country
     * @return CountryDTO
     */
    public static CountryDTO fromEntity(Country country) {
        if (country == null) {
            return null;
        }

        return CountryDTO.builder()
                .id(country.getId())
                .name(country.getName())
                .build();
    }

    /**
     * Преобразование списка сущностей в список DTO
     * @param countries
     * @return список DTO
     */
    public static List<CountryDTO> fromEntityList(List<Country> countries) {
        if (countries == null) {
            return null;
        }

        return countries.stream()
                .map(CountryDTO::fromEntity)
                .collect(Collectors.toList());
    }

}


