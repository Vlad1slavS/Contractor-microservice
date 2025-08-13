package io.github.contractormicroservice.controller.ui;

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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ui/industry")
@Tag(name = "UI Industry", description = "Защищенное API для работы со странами")
@Slf4j
public class UIIndustryController {

    private final IndustryService industryService;

    public UIIndustryController(IndustryService industryService) {
        this.industryService = industryService;
    }

    @Operation(summary = "Получение всех активных индустриальных кодов",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                   Получение всех активных индустриальных кодов с учетом ролевых ограничений:
                    **Доступ по ролям:**
                    - **USER** - может просматривать, но не редактировать, справочную информацию
                    - **CONTRACTOR_SUPERUSER** - повтор роли USER + возможность редактирования (сохранения и удаления)
                    - **SUPERUSER** - имеет полный доступ к сервису
                   """)
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
    @PreAuthorize("hasAnyRole('USER', 'CREDIT_USER', " +
            "'CONTRACTOR_RUS', 'CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @GetMapping("/all")
    public List<IndustryDTO> getAll() {
        log.info("Request to get all industries");
        return industryService.getAllActive();
    }

    @Operation(summary = "Получить индустриальный код по ID",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                   Получить индустриальный код по ID с учетом ролевых ограничений:
                   
                    **Доступ по ролям:**
                    - **USER** - может просматривать, но не редактировать, справочную информацию
                    - **CONTRACTOR_SUPERUSER** - повтор роли USER + возможность редактирования (сохранения и удаления)
                    - **SUPERUSER** - имеет полный доступ к сервису
                   """)
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
    @PreAuthorize("hasAnyRole('USER', 'CREDIT_USER', " +
            "'CONTRACTOR_RUS', 'CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @GetMapping("/{id}")
    public ResponseEntity<IndustryDTO> getOne(@PathVariable Long id) {
        log.info("Request to get industry by id: {}", id);
        IndustryDTO industryDTO = industryService.getOne(id);
        return ResponseEntity.ok(industryDTO);
    }

    @Operation(summary = "Удалить индустриальный код",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                    Удалить индустриальный код с учетом ролевых ограничений:
                    
                    **Доступ по ролям:**
                    - **CONTRACTOR_SUPERUSER** - имеет возможность удаления записей
                    - **SUPERUSER** - имеет полный доступ к сервису
                    """)
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
    @PreAuthorize("hasAnyRole('CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<IndustryDTO> delete(@PathVariable Long id) {
        log.info("Request to delete industry with id: {}", id);
        IndustryDTO deletedIndustry = industryService.deleteOne(id);
        log.info("Industry deleted: {}", deletedIndustry);
        return ResponseEntity.ok(deletedIndustry);
    }

    @Operation(summary = "Сохранить индустриальный код",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                    Сохранить индустриальный код с учетом ролевых ограничений:
                    
                    **Доступ по ролям:**
                    - **CONTRACTOR_SUPERUSER** - имеет возможность сохранения/редактирования записей
                    - **SUPERUSER** - имеет полный доступ к сервису
                    """)
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
    @PreAuthorize("hasAnyRole('CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @PutMapping("/save")
    public ResponseEntity<IndustryDTO> save(
            @Parameter(description = "Индустриальный код",
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
