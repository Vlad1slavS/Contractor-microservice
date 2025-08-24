package io.github.contractormicroservice.controller;

import io.github.contractormicroservice.model.dto.IndustryDTO;
import io.github.contractormicroservice.service.IndustryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Контроллер для работы с api индустриальных кодов
 */
@Slf4j
@RestController
@RequestMapping("api/v1/industries")
@Validated
@Tag(name = "Industries", description = "API для управления индустриальными кодами")
public class IndustryController {

    private final IndustryService industryService;

    public IndustryController(IndustryService industryService) {
        this.industryService = industryService;
    }

    @Operation(summary = "Получение всех активных индустриальных кодов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список индустриальных кодов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IndustryDTO.class),
                            examples = @ExampleObject(value = """
                                    [
                                    {
                                        "id": 1,
                                        "name": "Строительство"
                                    },
                                    {
                                        "id": 2,
                                        "name": "Транспорт"
                                    }
                                    ]
                                    """
                            )
                    )
            )

    })
    @GetMapping("/all")
    public List<IndustryDTO> getAll() {
        log.info("Request to get all industries");
        return industryService.getAllActive();
    }

    @Operation(summary = "Получить индустриальный код по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Индустриальный код найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IndustryDTO.class),
                            examples = @ExampleObject(
                                            value = """
                                            {
                                                "id": 1,
                                                "name": "Строительство"
                                            }
                                            """
                                    )
                            )
                    ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Индустриальный код не найден",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "Industry not found with id: 999",
                                        "timestamp": "timestamp"
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<IndustryDTO> getOne(@PathVariable Long id) {
        log.info("Request to get industry by id: {}", id);
        IndustryDTO industryDTO = industryService.getOne(id);
        return ResponseEntity.ok(industryDTO);
    }

    @Operation(summary = "Удалить индустриальный код")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Индустриальный код успешно удален",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IndustryDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "id": 1,
                                        "name": "Строительство"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Индустриальный код не найден",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "message": "Industry not found with id: 999",
                                        "timestamp": "timestamp"
                                    }
                                    """
                            )
                    )
            )
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<IndustryDTO> delete(@PathVariable Long id) {
        log.info("Request to delete industry with id: {}", id);
        IndustryDTO deletedIndustry = industryService.deleteOne(id);
        log.info("Industry deleted: {}", deletedIndustry);
        return ResponseEntity.ok(deletedIndustry);
    }

    @Operation(summary = "Сохранить индустриальный код")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Индустриальный код успешно сохранен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IndustryDTO.class),
                            examples = @ExampleObject(
                                    name = "Сохраненный индустриальный код",
                                    value = """
                                    {
                                        "id": 1,
                                        "name": "Строительство"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                {
                                    "error": "Ошибка валидации",
                                    "message": "Переданные данные не прошли валидацию",
                                    "timestamp": "timestamp",
                                    "validationErrors": [
                                        {
                                            "field": "fieldName",
                                            "error": "error message"
                                        }
                                    ]
                                }
                                """
                            )
                    )
            )
    })
    @PutMapping("/save")
    public ResponseEntity<IndustryDTO> save(
            @Parameter (description = "Индустриальный код",
                    required = true,
                    schema = @Schema(implementation = IndustryDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Индустриальный код",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IndustryDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Создание нового индустриального кода",
                                            value = """
                                            {
                                                "name": "Информационные технологии"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Обновление существующего кода",
                                            value = """
                            {
                                "id": 1,
                                "name": "Строительство и ремонт"
                            }
                            """
                                    )
                            }
                    )
            )

            @Valid @RequestBody IndustryDTO industryDTO) {
        log.info("Request to save industry: {}", industryDTO);

        IndustryDTO savedIndustry = industryService.save(industryDTO);
        log.info("Industry saved: {}", savedIndustry);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIndustry);
    }

}
