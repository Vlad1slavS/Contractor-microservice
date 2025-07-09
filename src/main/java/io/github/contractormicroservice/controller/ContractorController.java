package io.github.contractormicroservice.controller;

import io.github.contractormicroservice.model.dto.ContractorDTO;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.model.entity.Pagination;
import io.github.contractormicroservice.service.ContractorService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Контроллер для работы с api контрагентов
 */
@RestController
@RequestMapping("/api/v1/contractor")
@Slf4j
@Tag(name = "Contractors", description = "API для управления контрагентами")
public class ContractorController {

    private final ContractorService contractorService;

    public ContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
    }

    @Operation(summary = "Получить контрагента по ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контрагент найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractorDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "id": "CONTR123456",
                                               "parentId": null,
                                               "name": "Webbee",
                                               "nameFull": "Webbee the best <3",
                                               "inn": "7723456789",
                                               "ogrn": "1167746123456",
                                               "country": "RUS",
                                               "industry": {
                                                 "id": 1,
                                                 "name": "Производство"
                                                 },
                                               "orgForm": {
                                                 "id": 1,
                                                 "name": "ООО"
                                                 },
                                               "createDate": "timestamp",
                                               "modifyDate": "timestamp",
                                               "createUserId": "user_admin",
                                               "modifyUserId": "user_admin"
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Контрагент не найден",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "message": "Contractor not found with id: 999",
                                        "timestamp": "timestamp"
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Contractor> getOne(@PathVariable String id) {
        log.info("Request to get contractor by id: {}", id);
        Contractor contractor = contractorService.getOne(id);
        return ResponseEntity.ok(contractor);
    }

    @Operation(summary = "Удалить контрагента (логическое удаление)")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контрагент успешно удален",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractorDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                               "id": "CONTR123456",
                                               "parentId": null,
                                               "name": "TEST NAME",
                                               "nameFull": "TEST FULL NAME",
                                               "inn": "7723422789",
                                               "ogrn": "11565346123456",
                                               "country": "ABH",
                                               "industry": {
                                                 "id": 1,
                                                 "name": "Производство"
                                                 },
                                               "orgForm": {
                                                 "id": 1,
                                                 "name": "ООО"
                                                 },
                                               "createDate": "timestamp",
                                               "modifyDate": "timestamp",
                                               "createUserId": "user_admin",
                                               "modifyUserId": "user_admin"
                                             }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Контрагент не найден",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "message": "Contractor not found with id: 999",
                                        "timestamp": "timestamp"
                                    }
                                    """
                            )
                    )
            )
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Contractor> delete(@PathVariable String id) {
        log.info("Request to delete contractor with id: {}", id);
        Contractor deletedContractor = contractorService.deleteOne(id);
        log.info("Contractor deleted: {}", deletedContractor);
        return ResponseEntity.ok(deletedContractor);
    }

    @Operation(summary = "Сохранить или обновить контрагента")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Контрагент успешно сохранен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractorDTO.class),
                            examples = @ExampleObject(
                                    name = "Сохраненный контрагент",
                                    value = """
                                    {
                                               "id": "CONTR123456",
                                               "parentId": null,
                                               "name": "TEST NAME",
                                               "nameFull": "TEST FULL NAME",
                                               "inn": "7723422789",
                                               "ogrn": "11565346123456",
                                               "country": "ABH",
                                               "industry": {
                                                 "id": 1,
                                                 "name": "Производство"
                                                 },
                                               "orgForm": {
                                                 "id": 1,
                                                 "name": "ООО"
                                                 },
                                               "createDate": "timestamp",
                                               "modifyDate": "timestamp",
                                               "createUserId": "user_admin",
                                               "modifyUserId": "user_admin"
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
    public ResponseEntity<?> saveContractor(
            @Parameter(description = "Контрагент",
                    required = true,
                    schema = @Schema(implementation = ContractorDTO.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Контрагент",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractorDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Создание нового контрагента",
                                            value = """
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
                                    ),
                                    @ExampleObject(
                                            name = "Обновление существующего контрагента",
                                            value = """
                                            {
                                                "id": "TEST-123",
                                                "name": "TEST_NAME",
                                                "inn": "TEST_INN2",
                                                "ogrn": "TEST_OGRN2",
                                                "country": "RU",
                                                "industry": 3,
                                                "org_form": 5
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody ContractorDTO contractorDto) {
        log.info("Request to save contractor: {}", contractorDto);
        Contractor savedContractor = contractorService.save(contractorDto);
        log.info("Contractor saved: {}", savedContractor);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedContractor);
    }


    @Operation(summary = "Поиск контрагентов с пагинацией и фильтрами")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Контрагенты найдены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractorDTO.class),
                            examples = @ExampleObject(
                                    name = "Найденная страница с контрагентами",
                                    value = """
                                        {
                                          "contractors": [
                                            {
                                              "id": "CONTR123456",
                                              "parentId": null,
                                              "name": "TEST_NAME",
                                              "nameFull": "TEST_FULL_NAME",
                                              "inn": "7723422789",
                                              "ogrn": "11565346123456",
                                              "country": "ABH",
                                              "industry": {
                                                "id": 1,
                                                "name": "Производство"
                                              },
                                              "orgForm": {
                                                "id": 1,
                                                "name": "ООО"
                                              },
                                              "createDate": "timestamp",
                                              "modifyDate": "timestamp",
                                              "createUserId": "user_admin",
                                              "modifyUserId": "user_admin"
                                            }
                                          ],
                                          "page": 0,
                                          "limit": 10,
                                          "totalElements": 20,
                                          "hasNext": true,
                                          "hasPrevious": false
                                        }
                                    """
                            )
                    )
            )
    })
    @PostMapping("/search")
    public ResponseEntity<?> searchContractors(
            @Parameter(description = "Фильтр поиска",
                    schema = @Schema(implementation = ContractorFilter.class))
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Контрагент",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractorFilter.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Фильтр поиска",
                                            value = """
                                            {
                                                "contractorId": "TEST-123",
                                                "parentId": "TEST_PARENT_ID",
                                                "contractorSearch": "1234567890",
                                                "country": "RU",
                                                "industry": 7,
                                                "org_form": 2
                                            }
                                            """
                                    )
                            }
                    )
            )
            @RequestBody(required = false) ContractorFilter searchRequest,
                                               @RequestParam(defaultValue = "0") Integer page,
                                               @RequestParam(defaultValue = "10") Integer limit) {
        log.info("Request to search contractors with search request: {}", searchRequest);
        Pagination pagination = contractorService.searchContractors(searchRequest, page, limit);
        log.info("Contractors found: {}", pagination.getContractors().size());
        return ResponseEntity.ok(pagination);
    }

}
