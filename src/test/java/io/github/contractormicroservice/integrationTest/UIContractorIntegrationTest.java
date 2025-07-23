package io.github.contractormicroservice.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.repository.contractor.ContractorRepository;
import io.github.contractormicroservice.repository.country.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class UIContractorIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("contractor_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private CountryRepository countryRepository;

    @BeforeEach
    void setUp() {
        CountryDTO russiaCountry = CountryDTO.builder()
                .id("RUS")
                .name("Россия")
                .build();

        CountryDTO germanyCountry = CountryDTO.builder()
                .id("ARM")
                .name("Армения")
                .build();

        Contractor russianContractor1 = Contractor.builder()
                .id("CONTR001")
                .name("ООО Российская компания")
                .industry(2L)
                .country("RUS")
                .countryEntity(russiaCountry)
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .isActive(true)
                .isNew(true)
                .build();

        Contractor russianContractor2 = Contractor.builder()
                .id("CONTR002")
                .name("ЗАО Московская фирма")
                .country("RUS")
                .countryEntity(russiaCountry)
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .isActive(true)
                .isNew(true)
                .build();

        Contractor germanContractor = Contractor.builder()
                .id("CONTR003")
                .name("Deutsche Firma GmbH")
                .country("ARM")
                .countryEntity(germanyCountry)
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .isActive(true)
                .isNew(true)
                .build();

        contractorRepository.saveAll(List.of(russianContractor1, russianContractor2, germanContractor));
    }

    /**
     * Тест поиска контрагентов с ролью CONTRACTOR_RUS
     */
    @Test
    @WithMockUser(roles = "CONTRACTOR_RUS")
    void searchContractors_ContractorRusRole_OnlyRussianContractors() throws Exception {
        ContractorFilter filter = new ContractorFilter();
        filter.setContractorId("");

        mockMvc.perform(post("/api/v1/ui/contractor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.contractors", hasSize(2)))
                .andExpect(jsonPath("$.contractors[*].countryEntity.id", everyItem(is("RUS"))))
                .andExpect(jsonPath("$.contractors[*].name", containsInAnyOrder(
                        "ООО Российская компания",
                        "ЗАО Московская фирма")));
    }

    /**
     * Тест поиска по имени контрагента с ролью CONTRACTOR_RUS
     */
    @Test
    @WithMockUser(roles = "CONTRACTOR_RUS")
    void searchByName_ContractorRusRole_OnlyMatchingRussianContractors() throws Exception {
        ContractorFilter filter = new ContractorFilter();
        filter.setContractorSearch("Российская");

        mockMvc.perform(post("/api/v1/ui/contractor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.contractors", hasSize(1)))
                .andExpect(jsonPath("$.contractors[0].countryEntity.id", is("RUS")))
                .andExpect(jsonPath("$.contractors[0].name", is("ООО Российская компания")));
    }

    /**
     * Тест поиска контрагентов с ролью CONTRACTOR_SUPERUSER
     */
    @Test
    @WithMockUser(roles = "CONTRACTOR_SUPERUSER")
    void searchContractors_SuperuserRole_AllContractors() throws Exception {
        ContractorFilter filter = new ContractorFilter();

        mockMvc.perform(post("/api/v1/ui/contractor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter))
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.contractors", hasSize(3)))
                .andExpect(jsonPath("$.contractors[*].countryEntity.id", containsInAnyOrder("RUS", "RUS", "ARM")));
    }

    /**
     * Тест получения индустриального кода с ролью USER
     */
    @Test
    @WithMockUser(roles = "USER")
    void getIndustryByIdWithRoleUser_IndustryById() throws Exception {
        mockMvc.perform(get("/api/v1/ui/industry/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Авиастроение")));
    }

    /**
     * Тест попытки удаления страны с недостающими правами (USER)
     */
    @Test
    @WithMockUser(roles = "USER")
    void tryDeleteCountry_WithoutSuperUserRole_AccessDenied() throws Exception {
        mockMvc.perform(delete("/api/v1/ui/country/delete/{id}", "ARM"))
                .andExpect(status().isForbidden());
    }



}
