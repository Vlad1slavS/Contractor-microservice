package io.github.contractormicroservice.integration.cacheTest;

import io.github.contractormicroservice.model.dto.OrgFormDTO;
import io.github.contractormicroservice.model.entity.OrgForm;
import io.github.contractormicroservice.repository.country.CountryRepository;
import io.github.contractormicroservice.repository.industry.IndustryRepository;
import io.github.contractormicroservice.repository.orgForm.OrgFormRepository;
import io.github.contractormicroservice.service.ContractorService;
import io.github.contractormicroservice.service.OrgFormService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Интеграционный тест для проверки кэширования OrgFormService
 */
@SpringBootTest
@Testcontainers
class OrgFormServiceCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("orgForm_service_test_db")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private OrgFormService orgFormService;

    @MockitoBean
    private OrgFormRepository orgFormRepository;

    @MockitoBean
    private IndustryRepository industryRepository;

    @MockitoBean
    private CountryRepository countryRepository;

    @MockitoBean
    private ContractorService contractorService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("orgForms").clear();
        Mockito.reset(orgFormRepository);
    }

    @Test
    void doubleMethodCall_CallRepositoryOnceCacheFirstly() {

        List<OrgForm> orgForms = Arrays.asList(
                OrgForm.builder().id(1L).name("ООО").isActive(true).build(),
                OrgForm.builder().id(2L).name("ИП").isActive(true).build()
        );
        when(orgFormRepository.findAllActive()).thenReturn(orgForms);

        List<OrgFormDTO> result1 = orgFormService.getAllActive();
        List<OrgFormDTO> result2 = orgFormService.getAllActive();

        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(2);
        assertThat(result1.get(0).getName()).isEqualTo("ООО");

        verify(orgFormRepository, times(1)).findAllActive();
        assertThat(cacheManager.getCache("orgForms").get("all")).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictCacheOnSaveNewOrgForm() {

        List<OrgForm> orgForms = Arrays.asList(
                OrgForm.builder().id(1L).name("ООО").isActive(true).build()
        );
        when(orgFormRepository.findAllActive()).thenReturn(orgForms);

        OrgForm newOrgForm = OrgForm.builder().id(2L).name("АО").isActive(true).build();
        when(orgFormRepository.save(any(OrgForm.class))).thenReturn(newOrgForm);

        orgFormService.getAllActive();

        OrgFormDTO orgFormToSave = OrgFormDTO.builder().name("АО").build();
        orgFormService.save(orgFormToSave);

        orgFormService.getAllActive();

        verify(orgFormRepository, times(2)).findAllActive();
    }


    @Test
    void clearingCacheTest_EvictCacheOnDelete() {

        OrgForm orgForm = OrgForm.builder().id(1L).name("ООО").isActive(true).build();
        List<OrgForm> orgForms = Arrays.asList(orgForm);

        when(orgFormRepository.findAllActive()).thenReturn(orgForms);
        when(orgFormRepository.findById(1L)).thenReturn(Optional.of(orgForm));
        when(orgFormRepository.save(any(OrgForm.class))).thenReturn(orgForm);

        orgFormService.getAllActive();
        orgFormService.deleteOne(1L);
        orgFormService.getAllActive();

        verify(orgFormRepository, times(2)).findAllActive();
    }
}
