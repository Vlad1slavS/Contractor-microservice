package io.github.contractormicroservice.integration.cacheTest;

import io.github.contractormicroservice.model.dto.IndustryDTO;
import io.github.contractormicroservice.model.entity.Industry;
import io.github.contractormicroservice.repository.country.CountryRepository;
import io.github.contractormicroservice.repository.industry.IndustryRepository;
import io.github.contractormicroservice.repository.orgForm.OrgFormRepository;
import io.github.contractormicroservice.service.ContractorService;
import io.github.contractormicroservice.service.IndustryService;
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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

/**
 * Интеграционный тест для проверки кэширования IndustryService
 */
@SpringBootTest
@Testcontainers
class IndustryServiceCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("industry_service_test_db")
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
    private IndustryService industryService;

    @MockitoBean
    private IndustryRepository industryRepository;

    @MockitoBean
    private CountryRepository countryRepository;

    @MockitoBean
    private OrgFormRepository orgFormRepository;

    @MockitoBean
    private ContractorService contractorService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("industries").clear();
        Mockito.reset(industryRepository);

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var cache = cacheManager.getCache("industries");
                    assertThat(cache).isNotNull();
                    assertThat(cache.get("all")).isNull();
                });
    }

    @Test
    void doubleMethodCall_CallRepositoryOnceCacheFirstly() {

        List<Industry> industries = Arrays.asList(
                Industry.builder().id(1L).name("Строительство").isActive(true).build(),
                Industry.builder().id(2L).name("Авиастроение").isActive(true).build()
        );
        when(industryRepository.findAllActive()).thenReturn(industries);

        List<IndustryDTO> result1 = industryService.getAllActive();
        List<IndustryDTO> result2 = industryService.getAllActive();

        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(2);
        assertThat(result1.get(0).getName()).isEqualTo("Строительство");

        verify(industryRepository, times(1)).findAllActive();
        assertThat(cacheManager.getCache("industries").get("all")).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictCacheOnSaveNewIndustry() {

        List<Industry> industries = Arrays.asList(
                Industry.builder().id(1L).name("Строительство").isActive(true).build()
        );

        when(industryRepository.findAllActive()).thenReturn(industries);

        Industry newIndustry = Industry.builder().id(2L).name("Транспорт").isActive(true).build();
        when(industryRepository.save(any(Industry.class))).thenReturn(newIndustry);

        industryService.getAllActive();

        IndustryDTO industryToSave = IndustryDTO.builder().name("Транспорт").build();
        industryService.save(industryToSave);

        industryService.getAllActive();

        verify(industryRepository, times(2)).findAllActive();
    }

    @Test
    void  clearingCacheTest_EvictCacheOnDelete() {

        Industry industry = Industry.builder().id(1L).name("Строительство").isActive(true).build();
        List<Industry> industries = Arrays.asList(industry);

        when(industryRepository.findAllActive()).thenReturn(industries);
        when(industryRepository.findById(1L)).thenReturn(Optional.of(industry));
        when(industryRepository.save(any(Industry.class))).thenReturn(industry);

        industryService.getAllActive();
        industryService.deleteOne(1L);
        industryService.getAllActive();

        verify(industryRepository, times(2)).findAllActive();
    }
}
