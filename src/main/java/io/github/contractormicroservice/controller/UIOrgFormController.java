package io.github.contractormicroservice.controller;

import io.github.contractormicroservice.model.dto.OrgFormDTO;
import io.github.contractormicroservice.service.OrgFormService;
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
@RequestMapping("/api/v1/ui/orgForm")
@Tag(name = "UI Countries", description = "Защищенное API для работы со странами")
@Slf4j
public class UIOrgFormController {

    private final OrgFormService orgFormService;

    public UIOrgFormController(OrgFormService orgFormService) {
        this.orgFormService = orgFormService;
    }

    @Operation(summary = "Получение всех активных организационных форм")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Список организационных форм успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrgFormDTO.class),
                            examples = @ExampleObject(value = """
                                    [
                                    {
                                        "id": 1,
                                        "name": "ООО"
                                    },
                                    {
                                        "id": 2,
                                        "name": "ИП"
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
    public List<OrgFormDTO> getAll() {
        log.info("Request to get all org forms");
        return orgFormService.getAllActive();
    }

    @Operation(summary = "Получить организационную форму по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Организационная форма найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrgFormDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "id": 1,
                                        "name": "ООО"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Организационная форма не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "OrgForm not found with id: 999",
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
    public OrgFormDTO getOne(@PathVariable Long id) {
        log.info("Request to get org form by id: {}", id);
        return orgFormService.getOne(id);
    }

    @Operation(summary = "Удалить организационную форму")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Организационная форма успешно удалена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrgFormDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "id": 1,
                                        "name": "ООО"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Организационная форма не найдена",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "message": "OrgForm not found with id: 999",
                                        "timestamp": "timestamp"
                                    }
                                    """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @DeleteMapping("/delete/{id}")
    public OrgFormDTO delete(@PathVariable Long id) {
        log.info("Request to delete org form with id: {}", id);
        OrgFormDTO deletedOrgForm = orgFormService.deleteOne(id);
        log.info("Org form deleted: {}", deletedOrgForm);
        return deletedOrgForm;
    }

    @Operation(summary = "Сохранить организационную форму")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Организационная форма успешно сохранена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrgFormDTO.class),
                            examples = @ExampleObject(
                                    name = "Сохраненная организационная форма",
                                    value = """
                                    {
                                        "id": 1,
                                        "name": "ООО"
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
    public ResponseEntity<OrgFormDTO> save(
            @Parameter(description = "Организационная форма",
                    required = true,
                    schema = @Schema(implementation = OrgFormDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Организационная форма",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrgFormDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Создание новой организационной формы",
                                            value = """
                                            {
                                                "name": "ПАО"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Обновление существующей формы",
                                            value = """
                                            {
                                                "id": 1,
                                                "name": "ООО"
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody OrgFormDTO orgFormDTO) {
        log.info("Request to save org form: {}", orgFormDTO);
        OrgFormDTO savedOrgForm = orgFormService.save(orgFormDTO);
        log.info("Org form saved: {}", savedOrgForm);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrgForm);
    }

}
