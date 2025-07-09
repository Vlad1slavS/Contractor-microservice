package io.github.contractormicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object для контрагента (для ответа пользователю)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(
        name = "ContractorDTO",
        description = "Объект передачи данных контрагента",
        example = """
            {
                "id": "TEST-123",
                "name": "TEST_NAME",
                "name_full": "TEST_NAME_FULL",
                "inn": "TEST_INN",
                "ogrn": "TEST_OGRN",
                "country": "RU",
                "industry": 1,
                "org_form": 2
            }
            """
)
public class ContractorDTO {

    @Schema(
            description = "Уникальный идентификатор контрагента",
            example = "TEST-123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "ID контрагента не может быть null")
    private String id;

    @Schema(
            description = "Уникальный идентификатор родительского контрагента",
            example = "TEST-PARENT-123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonProperty("parent_id")
    private String parentId;

    @Schema(
            description = "Название контрагента",
            example = "TEST-NAME-123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Название страны не может быть пустым")
    private String name;

    @Schema(
            description = "Полное название контрагента",
            example = "TEST-NAME-FULL-123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonProperty("name_full")
    private String nameFull;

    @Schema(
            description = "ИНН контрагента",
            example = "1234567890",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 10, max = 12, message = "ИНН должен содержать от 10 до 12 цифр")
    private String inn;

    @Schema(
            description = "ОГРН контрагента",
            example = "1234567890125",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(min = 13, max = 13, message = "ОГРН должен содержать ровно 13 цифр")
    private String ogrn;

    @Schema(
            description = "Страна контрагента",
            example = "RU",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Страна не может быть пустой")
    private String country;

    @Schema(
            description = "Индустриальный код контрагента",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Индустриальный код не может быть null")
    private Long industry;

    @Schema(
            description = "Организационная форма контрагента",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("org_form")
    @NotNull(message = "Организационная форма не может быть null")
    private Long orgForm;

}
