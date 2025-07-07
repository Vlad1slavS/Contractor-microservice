package io.github.contractormicroservice.integrationTest;

import io.github.contractormicroservice.model.entity.OrgForm;
import io.github.contractormicroservice.repository.orgForm.OrgFormRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class OrgFormIntegrationTest {

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
    private OrgFormRepository orgFormRepository;

    @BeforeEach
    void setUp() {
        orgFormRepository.deleteAll();
    }

    @Test
    void findAllActiveOrgForms_ReturnsOnlyActiveRecords() {
        OrgForm activeOrgForm1 = OrgForm.builder()
                .name("Нотариус")
                .isActive(true)
                .build();

        OrgForm activeOrgForm2 = OrgForm.builder()
                .name("Адвокат")
                .isActive(true)
                .build();

        OrgForm inactiveOrgForm = OrgForm.builder()
                .name("Неактивная огр форма")
                .isActive(false)
                .build();

        orgFormRepository.createOrgForm(activeOrgForm1);
        orgFormRepository.createOrgForm(activeOrgForm2);
        orgFormRepository.createOrgForm(inactiveOrgForm);

        List<OrgForm> activeOrgForms = orgFormRepository.findAllActive();

        assertThat(activeOrgForms).hasSize(2);
        assertThat(activeOrgForms)
                .extracting(OrgForm::getName)
                .containsExactlyInAnyOrder("Нотариус", "Адвокат");
    }

    @Test
    void createOrgForm_Success() {
        OrgForm newOrgForm = OrgForm.builder()
                .name("Нотариус")
                .build();

        OrgForm createdOrgForm = orgFormRepository.createOrgForm(newOrgForm);

        assertThat(createdOrgForm).isNotNull();
        assertThat(createdOrgForm.getId()).isNotNull();
        assertThat(createdOrgForm.getName()).isEqualTo("Нотариус");

        Optional<OrgForm> foundOrgForm = orgFormRepository.findById(createdOrgForm.getId());
        assertThat(foundOrgForm).isPresent();
        assertThat(foundOrgForm.get().getName()).isEqualTo("Нотариус");
    }

    @Test
    void findById_ReturnsOrgForm() {

        OrgForm orgForm = OrgForm.builder()
                .name("Нотариус")
                .build();

        OrgForm orgForm2 = OrgForm.builder()
                .name("Адвокат")
                .build();

        orgFormRepository.createOrgForm(orgForm);
        OrgForm createdOrgForm = orgFormRepository.createOrgForm(orgForm2);

        Optional<OrgForm> result = orgFormRepository.findById(createdOrgForm.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Адвокат");
    }

    @Test
    void findById_NonExistentId_ReturnsEmpty() {

        Optional<OrgForm> result = orgFormRepository.findById(99999L);
        assertThat(result).isEmpty();

    }

    @Test
    void createOrgForm_WithNullName() {
        OrgForm orgFormWithNullName = OrgForm.builder()
                .name(null)
                .build();

        assertThatThrownBy(() -> orgFormRepository.createOrgForm(orgFormWithNullName))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void createOrgForm_WithEmptyName() {
        OrgForm orgFormWithEmptyName = OrgForm.builder()
                .name("")
                .build();

        OrgForm createdOrgForm = orgFormRepository.createOrgForm(orgFormWithEmptyName);

        assertThat(createdOrgForm).isNotNull();
        assertThat(createdOrgForm.getId()).isNotNull();
        assertThat(createdOrgForm.getName()).isEmpty();
    }
}
