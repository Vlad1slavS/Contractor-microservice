package io.github.contractormicroservice.controller;

import io.github.contractormicroservice.model.dto.ContractorDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ui/contractor")
@Slf4j
@Tag(name = "UI Contractors", description = "Защищенное API для работы с контрагентами")
public class UIContractorController {

    private final ContractorService contractorService;

    public UIContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
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
    @PreAuthorize("hasAnyRole('CONTRACTOR_RUS', 'CONTRACTOR_SUPERUSER', 'SUPERUSER')")
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

        log.info("UI Request to search contractors: {}", searchRequest);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isRus = false;
        boolean isSuper = false;

        for (GrantedAuthority authority : auth.getAuthorities()) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_CONTRACTOR_RUS")) {
                isRus = true;
            }

            if (role.equals("ROLE_CONTRACTOR_SUPERUSER") || role.equals("ROLE_SUPERUSER")) {
                isSuper = true;
            }
        }

        if (isRus && !isSuper) {
            if (searchRequest == null) {
                searchRequest = new ContractorFilter();
            }
            searchRequest.setCountry("RUS");
        }

        Pagination pagination = contractorService.searchContractors(searchRequest, page, limit);
        return ResponseEntity.ok(pagination);
    }

}
