package io.github.contractormicroservice.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.config.SecurityConfig;
import io.github.contractormicroservice.controller.ui.UICountryController;
import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.model.entity.Pagination;
import io.github.contractormicroservice.service.ContractorService;
import io.github.contractormicroservice.service.CountryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UICountryController.class)
@Import({SecurityConfig.class})
public class UIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ContractorService contractorService;

    @MockitoBean
    private CountryService countryService;

    private CountryDTO country;

    @BeforeEach
    void setUp() {
        country = CountryDTO.builder()
                .id("RUS")
                .name("Россия")
                .build();
    }

    /**
     * Тест получения всех стран с ролью USER
     */
    @Test
    public void getAllCountries_AllCountries() throws Exception {
        List<CountryDTO> countries = Arrays.asList(
                CountryDTO.builder().id("RU").name("Россия").build(),
                CountryDTO.builder().id("CN").name("Китай").build(),
                CountryDTO.builder().id("DE").name("Германия").build()
        );

        when(countryService.getAllActive()).thenReturn(countries);

        mockMvc.perform(get("/api/v1/ui/contractor/country/all")
                        .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is("RU")))
                .andExpect(jsonPath("$[0].name", is("Россия")))
                .andExpect(jsonPath("$[1].id", is("CN")))
                .andExpect(jsonPath("$[1].name", is("Китай")))
                .andExpect(jsonPath("$[2].id", is("DE")))
                .andExpect(jsonPath("$[2].name", is("Германия")));

        verify(countryService, times(1)).getAllActive();
    }

    /**
     * Тест получения всех стран с ролью CREDIT_USER
     */
    @Test
    @WithMockUser(roles = "CREDIT_USER")
    public void getAllCountries_WithCreditUserRole_AllCountries() throws Exception {
        List<CountryDTO> countries = Arrays.asList(
                CountryDTO.builder().id("RU").name("Россия").build()
        );

        when(countryService.getAllActive()).thenReturn(countries);

        mockMvc.perform(get("/api/v1/ui/contractor/country/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("RU")))
                .andExpect(jsonPath("$[0].name", is("Россия")));

        verify(countryService, times(1)).getAllActive();
    }

    /**
     * Тест для получения всех стран с несуществующей ролью
     */
    @Test
    @WithMockUser(roles = "MODERATOR")
    public void getAllCountries_WithModeratorRole_AсcessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/ui/contractor/country/all"))
                .andExpect(status().isForbidden());
    }

    /**
     * Тест получения всех стран с ролью SUPERUSER
     */
    @Test
    @WithMockUser(roles = "SUPERUSER")
    public void getAllCountries_WithSuperuserRole_AllCountries() throws Exception {
        List<CountryDTO> countries = Arrays.asList(
                CountryDTO.builder().id("RU").name("Россия").build(),
                CountryDTO.builder().id("US").name("США").build()
        );

        when(countryService.getAllActive()).thenReturn(countries);

        mockMvc.perform(get("/api/v1/ui/contractor/country/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("RU")))
                .andExpect(jsonPath("$[0].name", is("Россия")))
                .andExpect(jsonPath("$[1].id", is("US")))
                .andExpect(jsonPath("$[1].name", is("США")));

        verify(countryService, times(1)).getAllActive();
    }

    /**
     * Тест поиска контрагентов с ролью CONTRACTOR_SUPERUSER
     */
    @Test
    @WithMockUser(roles = "CONTRACTOR_SUPERUSER")
    public void searchContractors_WithContrSuperuserRole_Pagination() throws Exception {
        ContractorFilter filter = new ContractorFilter();
        filter.setContractorId("Test123");

        Pagination pagination = new Pagination();
        pagination.setTotalElements(1);

        when(contractorService.searchContractors(any(ContractorFilter.class), eq(0), eq(10)))
                .thenReturn(pagination);

        mockMvc.perform(post("/api/v1/ui/contractor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(1)));

        verify(contractorService, times(1)).searchContractors(any(ContractorFilter.class), eq(0), eq(10));
    }

    /**
     * Тест поиска контрагентов с ролью CONTRACTOR_RUS
     */
    @Test
    @WithMockUser(roles = "CONTRACTOR_RUS")
    public void searchContractors_WithContractorRusRole_PaginationWithRusPages() throws Exception {
        ContractorFilter filter = new ContractorFilter();
        filter.setContractorId("Test123");

        Contractor expectedContractor = Contractor.builder()
                .id("TEST_ID")
                .name("TEST_NAME")
                .countryEntity(country)
                .build();

        Pagination pagination = new Pagination();
        pagination.setContractors(List.of(expectedContractor));
        pagination.setTotalElements(1);

        when(contractorService.searchContractors(any(ContractorFilter.class), eq(0), eq(10)))
                .thenReturn(pagination);

        mockMvc.perform(post("/api/v1/ui/contractor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.contractors[0].id", is("TEST_ID")))
                .andExpect(jsonPath("$.contractors[0].name", is("TEST_NAME")))
                .andExpect(jsonPath("$.contractors[0].countryEntity.id", is("RUS")));

        verify(contractorService, times(1)).searchContractors(argThat(request ->
                request.getCountry().equals("RUS")), eq(0), eq(10));
    }

}