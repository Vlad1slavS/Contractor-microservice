package io.github.contractormicroservice.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.model.entity.Pagination;
import io.github.contractormicroservice.repository.contractor.ContractorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ContractorIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("contractor_db")
            .withUsername("contractor")
            .withPassword("1234");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContractorRepository contractorRepository;

    @BeforeEach
    void setUp() {

        Contractor contractor = Contractor.builder()
                .id("contractor-1")
                .name("ООО Рога и Копыта")
                .nameFull("Общество с ограниченной ответственностью Рога и Копыта")
                .inn("1234567890")
                .ogrn("123456789012345")
                .country("ABH")
                .industry(3L)
                .orgForm(2L)
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .isActive(true)
                .isNew(true)
                .build();

        Contractor contractor2 = Contractor.builder()
                .id("contractor-2")
                .name("Webbee the best <3")
                .nameFull("Webbee the best <3")
                .inn("1234567890")
                .ogrn("123456789012345")
                .country("AUS")
                .industry(3L)
                .orgForm(4L)
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .isActive(true)
                .isNew(true)
                .build();

        Contractor inactiveContractor = Contractor.builder()
                .id("contractor-3")
                .name("Inactive contractor")
                .nameFull("Inactive contractor")
                .country("BEL")
                .orgForm(2L)
                .industry(3L)
                .inn("1234567890")
                .isActive(false)
                .createDate(LocalDateTime.now())
                .modifyDate(LocalDateTime.now())
                .isNew(true)
                .build();

        contractorRepository.save(contractor);
        contractorRepository.save(contractor2);
        contractorRepository.save(inactiveContractor);
    }

    @AfterEach
    void tearDown() {
        contractorRepository.deleteAll();
    }

    /**
     * Тест поиска контрагента по id (со всеми связанными сущностями)
     */
    @Test
    void findByIdWithDetails_ExistingContractor() {
        String contractorId = "contractor-1";

        Optional<Contractor> result = contractorRepository.findByIdWithDetails(contractorId);

        assertThat(result).isPresent();
        Contractor contractor = result.get();

        assertThat(contractor.getId()).isEqualTo("contractor-1");
        assertThat(contractor.getName()).isEqualTo("ООО Рога и Копыта");
        assertThat(contractor.getNameFull()).isEqualTo("Общество с ограниченной ответственностью Рога и Копыта");
        assertThat(contractor.getInn()).isEqualTo("1234567890");
        assertThat(contractor.getOgrn()).isEqualTo("123456789012345");
        assertThat(contractor.getParentId()).isNull();
        assertThat(contractor.getIsActive()).isTrue();

        assertThat(contractor.getCountryEntity()).isNotNull();
        assertThat(contractor.getCountryEntity().getId()).isEqualTo("ABH");
        assertThat(contractor.getCountryEntity().getName()).isEqualTo("Абхазия");

        assertThat(contractor.getIndustryEntity()).isNotNull();
        assertThat(contractor.getIndustryEntity().getId()).isEqualTo(3L);
        assertThat(contractor.getIndustryEntity().getName()).isEqualTo("Автомобильный транспорт");

        assertThat(contractor.getOrgFormEntity()).isNotNull();
        assertThat(contractor.getOrgFormEntity().getId()).isEqualTo(2L);
        assertThat(contractor.getOrgFormEntity().getName()).isEqualTo("Автономная некоммерческая организация");
    }

    /**
     * Тест поиска контрагента по id (со всеми связанными сущностями) по несуществующему id
     */
    @Test
    void findByIdWithDetails_NonExistentContractor() {
        String contractorId = "non-existent-id";

        Optional<Contractor> result = contractorRepository.findByIdWithDetails(contractorId);

        assertThat(result).isEmpty();
    }

    /**
     * Тест поиска контрагентов с пагинацией без фильтров
     */
    @Test
    void searchContractors_NoFilters() {
        ContractorFilter filter = null;
        int page = 0;
        int limit = 10;

        Pagination result = contractorRepository.searchContractors(filter, page, limit);

        assertThat(result).isNotNull();
        assertThat(result.getContractors()).hasSize(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getLimit()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    /**
     * Тест поиска контрагентов с пагинацией с фильтром по ContractorId
     */
    @Test
    void searchContractors_FilterByContractorId() {
        ContractorFilter filter = new ContractorFilter();
        filter.setContractorId("contractor-1");

        Pagination result = contractorRepository.searchContractors(filter, 0, 10);

        assertThat(result.getContractors()).hasSize(1);
        assertThat(result.getContractors().getFirst().getId()).isEqualTo("contractor-1");
    }

    /**
     * Тест поиска контрагентов с пагинацией с несколькими фильтрами (contractor_search)
     */
    @Test
    void searchContractors_MultiFilters() {
        ContractorFilter filter = new ContractorFilter();
        filter.setCountry("ABH");
        filter.setIndustry(3);
        filter.setContractorSearch("Рога");

        Pagination result = contractorRepository.searchContractors(filter, 0, 10);

        assertThat(result.getContractors()).hasSize(1);
        Contractor contractor = result.getContractors().getFirst();
        assertThat(contractor.getCountry()).isEqualTo("ABH");
        assertThat(contractor.getIndustry()).isEqualTo(3L);
        assertThat(contractor.getName()).contains("Рога");
    }

    /**
     * Тест с пагинацией и фильтром по имени (но частичному совпадению)
     */
    @Test
    void searchContractors_PartialNameMatchFilter() {
        ContractorFilter filter = new ContractorFilter();
        filter.setContractorSearch("рога");

        Pagination result = contractorRepository.searchContractors(filter, 0, 10);

        assertThat(result.getContractors()).hasSize(1);
        assertThat(result.getContractors().getFirst().getName()).contains("Рога");
    }

    /**
     * Тест поиска контрагентов с пагинацией с фильтром по Industry (с нулевым значением)
     */
    @Test
    void searchContractors_IndustryZeroValue() {
        ContractorFilter filter = new ContractorFilter();
        filter.setIndustry(0);

        Pagination result = contractorRepository.searchContractors(filter, 0, 10);

        assertThat(result.getContractors()).hasSize(2);
    }

}
