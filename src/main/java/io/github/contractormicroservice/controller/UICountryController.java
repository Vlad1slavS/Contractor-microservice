package io.github.contractormicroservice.controller;

import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ui/contractor")
@Tag(name = "UI Countries", description = "Защищенное API для работы со странами")
@Slf4j
public class UICountryController {

    private final CountryService countryService;

    public UICountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @Operation(summary = "Получение всех активных стран")
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
    @PreAuthorize("hasAnyRole('USER', 'CREDIT_USER', 'OVERDRAFT_USER', " +
            "'DEAL_SUPERUSER', 'CONTRACTOR_RUS', 'CONTRACTOR_SUPERUSER', 'SUPERUSER')")
    @GetMapping("/country/all")
    public List<CountryDTO> getAllCountries() {
        log.info("UI Request to get all countries");
        return countryService.getAllActive();
    }

}
