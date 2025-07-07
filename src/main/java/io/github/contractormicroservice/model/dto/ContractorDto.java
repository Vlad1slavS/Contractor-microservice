package io.github.contractormicroservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object для контрагента (для ответа пользователю)
 */
@Data
@NoArgsConstructor
public class ContractorDto {

    private String id;

    @JsonProperty("parent_id")
    private String parentId;

    @NotBlank(message = "Название страны не может быть пустым")
    private String name;

    @JsonProperty("name_full")
    private String nameFull;

    @Size(min = 10, max = 12, message = "ИНН должен содержать от 10 до 12 цифр")
    private String inn;

    @Size(min = 13, max = 13, message = "ОГРН должен содержать ровно 13 цифр")
    private String ogrn;

    @NotBlank(message = "Страна не может быть пустой")
    private String country;

    @NotBlank(message = "Индустриальный код не может быть пустой")
    private Integer industry;

    @JsonProperty("org_form")
    @NotBlank(message = "Организационная форма не может быть пустой")
    private Integer orgForm;


}
