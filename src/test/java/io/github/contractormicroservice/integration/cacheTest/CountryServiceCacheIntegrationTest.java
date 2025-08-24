package io.github.contractormicroservice.integration.cacheTest;

import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.entity.Country;
import io.github.contractormicroservice.repository.country.CountryRepository;
import io.github.contractormicroservice.repository.industry.IndustryRepository;
import io.github.contractormicroservice.repository.orgForm.OrgFormRepository;
import io.github.contractormicroservice.service.ContractorService;
import io.github.contractormicroservice.service.CountryService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

/**
 * Интеграционный тест для проверки кэширования CountryService
 */
@SpringBootTest
@Testcontainers
class CountryServiceCacheIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("country_service_test_db")
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
    private CountryService countryService;

    @MockitoBean
    private CountryRepository countryRepository;

    @MockitoBean
    private IndustryRepository industryRepository;

    @MockitoBean
    private OrgFormRepository orgFormRepository;

    @MockitoBean
    private ContractorService contractorService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {

        cacheManager.getCache("countries").clear();
        Mockito.reset(countryRepository);

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    var cache = cacheManager.getCache("countries");
                    assertThat(cache).isNotNull();
                    assertThat(cache.get("all")).isNull();
                });
    }

    @Test
    void doubleMethodCall_CallRepositoryOnce() {

        List<Country> countries = Arrays.asList(
                Country.builder().id("RU").name("Россия").isActive(true).build(),
                Country.builder().id("US").name("США").isActive(true).build()
        );
        when(countryRepository.findAllActive()).thenReturn(countries);

        List<CountryDTO> result1 = countryService.getAllActive();
        List<CountryDTO> result2 = countryService.getAllActive();

        assertThat(result1).hasSize(2);
        assertThat(result2).hasSize(2);
        assertThat(result1.getFirst().getName()).isEqualTo("Россия");

        verify(countryRepository, times(1)).findAllActive();

        assertThat(cacheManager.getCache("countries").get("all")).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictCacheOnSave() {

        List<Country> countries = Arrays.asList(
                Country.builder().id("RU").name("Россия").isActive(true).build()
        );
        when(countryRepository.findAllActive()).thenReturn(countries);
        when(countryRepository.findById("DE")).thenReturn(java.util.Optional.empty());
        when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> invocation.getArgument(0));

        countryService.getAllActive();
        verify(countryRepository, times(1)).findAllActive();

        CountryDTO newCountry = CountryDTO.builder().id("DE").name("Германия").build();
        countryService.save(newCountry);

        countryService.getAllActive();

        verify(countryRepository, times(2)).findAllActive();

        assertThat(cacheManager.getCache("countries").get("all")).isNotNull();
    }

    @Test
    void clearingCacheTest_EvictCacheOnDelete() {

        Country existingCountry = Country.builder().id("RU").name("Россия").isActive(true).build();
        List<Country> countries = Arrays.asList(existingCountry);

        when(countryRepository.findAllActive()).thenReturn(countries);
        when(countryRepository.findById("RU")).thenReturn(java.util.Optional.of(existingCountry));
        when(countryRepository.save(any(Country.class))).thenAnswer(invocation -> invocation.getArgument(0));

        countryService.getAllActive();
        verify(countryRepository, times(1)).findAllActive();

        countryService.deleteOne("RU");

        countryService.getAllActive();

        verify(countryRepository, times(2)).findAllActive();
    }

    @Test
    void dontCacheTest_NotCacheIndividualCountryRequests() {

        Country country = Country.builder().id("RU").name("Россия").isActive(true).build();
        when(countryRepository.findById("RU")).thenReturn(java.util.Optional.of(country));

        countryService.getOne("RU");
        countryService.getOne("RU");

        verify(countryRepository, times(2)).findById("RU");

    }
}