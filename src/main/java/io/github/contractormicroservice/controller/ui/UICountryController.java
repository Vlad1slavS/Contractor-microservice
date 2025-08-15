package io.github.contractormicroservice.controller.ui;

import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.service.CountryService;
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
@RequestMapping("/api/v1/ui/country")
@Tag(name = "UI Countries", description = "Защищенное API для работы со странами")
@Slf4j
public class UICountryController {

    private final CountryService countryService;

    public UICountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @Operation(summary = "Получение всех активных стран",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                   Получение всех активных стран с учетом ролевых ограничений:
                    **Доступ по ролям:**
                    - **USER** - может просматривать, но не редактировать, справочную информацию
                    - **CONTRACTOR_SUPERUSER** - повтор роли USER + возможность редактирования (сохранения и удаления)
                    - **SUPERUSER** - имеет полный доступ к сервису
                   """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список стран успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryDTO.class),
                            examples = @ExampleObject(value = """
                                    [
                                    {
                                        "id": "RU",
                                        "name": "Россия"
                                    },
                                    {
                                        "id": "CN",
                                        "name": "Китай"
                                    }
                                    ]
                                    """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'CREDIT_USER', " +
            "'CONTRACTOR_RUS', 'CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @GetMapping("/country/all")
    public List<CountryDTO> getAllCountries() {
        log.info("UI Request to get all countries");
        return countryService.getAllActive();
    }

    @Operation(summary = "Получить страну по ID",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                   Получить страну по ID с учетом ролевых ограничений:
                    **Доступ по ролям:**
                    - **USER** - может просматривать, но не редактировать, справочную информацию
                    - **CONTRACTOR_SUPERUSER** - повтор роли USER + возможность редактирования (сохранения и удаления)
                    - **SUPERUSER** - имеет полный доступ к сервису
                   """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Страна найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "id": "RU",
                                                "name": "Россия"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Страна не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "Country not found with id: 999",
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
    public ResponseEntity<CountryDTO> getOne(@PathVariable String id) {
        log.info("UI Request to get country by id: {}", id);
        CountryDTO country = countryService.getOne(id);
        return ResponseEntity.ok(country);
    }

    @Operation(summary = "Удалить страну",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                    Удалить страну с учетом ролевых ограничений:
                    **Доступ по ролям:**
                    - **CONTRACTOR_SUPERUSER** - имеет возможность удаления записей
                    - **SUPERUSER** - имеет полный доступ к сервису
                    """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Страна успешно удалена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "id": "RU",
                                        "name": "Россия"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Страна не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "message": "Country not found with id: 999",
                                        "timestamp": "timestamp"
                                    }
                                    """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CountryDTO> delete(@PathVariable String id) {
        log.info("Request to delete country with id: {}", id);
        CountryDTO deletedCountry = countryService.deleteOne(id);
        log.info("Country deleted: {}", deletedCountry);
        return ResponseEntity.ok(deletedCountry);
    }

    @Operation(summary = "Сохранить страну",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth"),
            description = """
                    Сохранить страну с учетом ролевых ограничений:
                    **Доступ по ролям:**
                    - **CONTRACTOR_SUPERUSER** - имеет возможность сохранения записей
                    - **SUPERUSER** - имеет полный доступ к сервису
                    """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Страна успешно сохранена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryDTO.class),
                            examples = @ExampleObject(
                                    name = "Сохраненная страна",
                                    value = """
                                    {
                                        "id": "RU",
                                        "name": "Россия"
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
    @PreAuthorize("hasAnyRole('CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    public ResponseEntity<CountryDTO> save(
            @Parameter(description = "Страна",
                    required = true,
                    schema = @Schema(implementation = CountryDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Страна",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Создание новой страны",
                                            value = """
                                            {
                                                "id": "DE",
                                                "name": "Германия"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Обновление существующей страны",
                                            value = """
                                            {
                                                "id": "RU",
                                                "name": "Российская Федерация"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody CountryDTO countryDTO) {
        log.info("Request to save country: {}", countryDTO);

        CountryDTO savedCountry = countryService.save(countryDTO);
        log.info("Country saved: {}", savedCountry);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCountry);
    }

}
