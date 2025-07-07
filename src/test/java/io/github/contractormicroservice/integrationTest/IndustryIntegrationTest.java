package io.github.contractormicroservice.integrationTest;


import io.github.contractormicroservice.model.entity.Industry;
import io.github.contractormicroservice.repository.industry.IndustryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class IndustryIntegrationTest {

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
    private IndustryRepository industryRepository;

    @BeforeEach
    void setUp() {
        industryRepository.deleteAll();
    }

    @Test
    void findAllActiveIndustries_ReturnsOnlyActiveRecords() {
        Industry activeIndustry1 = Industry.builder()
                .name("Индустрия 1")
                .isActive(true)
                .build();

        Industry activeIndustry2 = Industry.builder()
                .name("Индустрия 2")
                .isActive(true)
                .build();

        Industry inactiveIndustry = Industry.builder()
                .name("Неактивная индустрия")
                .isActive(false)
                .build();

        industryRepository.createIndustry(activeIndustry1);
        industryRepository.createIndustry(activeIndustry2);
        industryRepository.createIndustry(inactiveIndustry);

        List<Industry> activeIndustries = industryRepository.findAllActive();

        assertThat(activeIndustries).hasSize(2);
        assertThat(activeIndustries)
                .extracting(Industry::getName)
                .containsExactlyInAnyOrder("Индустрия 1", "Индустрия 2");
    }

    @Test
    void createIndustry_Success() {
        Industry newIndustry = Industry.builder()
                .name("Новая индустрия")
                .build();

        Industry createdIndustry = industryRepository.createIndustry(newIndustry);

        assertThat(createdIndustry).isNotNull();
        assertThat(createdIndustry.getId()).isNotNull();
        assertThat(createdIndustry.getName()).isEqualTo("Новая индустрия");

        Optional<Industry> foundIndustry = industryRepository.findById(createdIndustry.getId());
        assertThat(foundIndustry).isPresent();
        assertThat(foundIndustry.get().getName()).isEqualTo("Новая индустрия");
    }

    @Test
    void findById_ReturnsIndustry() {

        Industry industry = Industry.builder()
                .name("Новая индустрия")
                .build();

        Industry industry2 = Industry.builder()
                .name("Новая индустрия 2")
                .build();

        industryRepository.createIndustry(industry);
        Industry createdIndustry = industryRepository.createIndustry(industry2);

        Optional<Industry> result = industryRepository.findById(createdIndustry.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Новая индустрия 2");
    }

    @Test
    void findById_NonExistentId_ReturnsEmpty() {

        Optional<Industry> result = industryRepository.findById(99999L);
        assertThat(result).isEmpty();

    }

    @Test
    void createIndustry_WithNullName() {
        Industry industryWithNullName = Industry.builder()
                .name(null)
                .build();

        assertThatThrownBy(() -> industryRepository.createIndustry(industryWithNullName))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void createIndustry_WithEmptyName() {
        Industry industryWithEmptyName = Industry.builder()
                .name("")
                .build();

        Industry createdIndustry = industryRepository.createIndustry(industryWithEmptyName);

        assertThat(createdIndustry).isNotNull();
        assertThat(createdIndustry.getId()).isNotNull();
        assertThat(createdIndustry.getName()).isEmpty();
    }

}
