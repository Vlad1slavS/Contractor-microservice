package io.github.contractormicroservice.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.entity.Country;
import io.github.contractormicroservice.repository.country.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CountryIntegrationTest {

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
    private CountryRepository countryRepository;

    @BeforeEach
    void setUp() {
        countryRepository.deleteAll();
    }

    @Test
    void getAllCountries_ShouldReturnAllActiveCountriesFromDatabase() throws Exception {

        Country russia = Country.builder()
                .id("RU")
                .name("Россия")
                .build();

        Country china = Country.builder()
                .id("CN")
                .name("Китай")
                .build();

        Country ukraine = Country.builder()
                .id("UA")
                .name("Украина")
                .isActive(false)
                .build();

        countryRepository.createCountry(russia);
        countryRepository.createCountry(china);
        countryRepository.createCountry(ukraine);

        List<Country> activeIndustries = countryRepository.findAllActive();

        assertThat(activeIndustries).hasSize(2);
        assertThat(activeIndustries)
                .extracting(Country::getName)
                .containsExactlyInAnyOrder("Россия", "Китай");

        assertThat(activeIndustries)
                .extracting(Country::getId)
                .containsExactlyInAnyOrder("RU", "CN");
    }

    @Test
    void getCountryById_ShouldReturnCountryFromDatabase() throws Exception {
        Country russia = Country.builder()
                .id("RU")
                .name("Россия")
                .isActive(true)
                .build();

        countryRepository.createCountry(russia);

        Country country = countryRepository.findById("RU").orElse(null);
        assertThat(country).isNotNull();
        assertThat(country.getId()).isNotNull();
        assertThat(country.getName()).isEqualTo("Россия");
        assertThat(country.isActive()).isTrue();

        Optional<Country> savedCountry = countryRepository.findById("RU");
        assertThat(savedCountry).isPresent();
        assertThat(savedCountry.get().getName()).isEqualTo("Россия");
        assertThat(savedCountry.get().isActive()).isTrue();
    }

    @Test
    void saveCountry_ShouldUpdateCountry() throws Exception {

        Country existingCountry = Country.builder()
                .id("RU")
                .name("Россия")
                .isActive(true)
                .build();

        countryRepository.createCountry(existingCountry);

        CountryDTO countryDTO = CountryDTO.builder()
                .id("RU")
                .name("РФ")
                .build();

        mockMvc.perform(put("/api/v1/country/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(countryDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("RU")))
                .andExpect(jsonPath("$.name", is("РФ")));

        Optional<Country> savedCountry = countryRepository.findById("RU");
        assertThat(savedCountry).isPresent();
        assertThat(savedCountry.get().getName()).isEqualTo("РФ");
        assertThat(savedCountry.get().isActive()).isTrue();

    }

    @Test
    void saveCountry_ShouldCreateNewCountry() throws Exception {

        Country newCountry = Country.builder()
                .id("FR")
                .name("Франция")
                .build();

        countryRepository.createCountry(newCountry);

        Optional<Country> savedCountry = countryRepository.findById("FR");
        assertThat(savedCountry).isPresent();
        assertThat(savedCountry.get().getName()).isEqualTo("Франция");
        assertThat(savedCountry.get().isActive()).isTrue();

    }

    @Test
    void deleteCountry_ShouldLogicallyDeleteCountry() throws Exception {

        Country countryToDelete = Country.builder()
                .id("RU")
                .name("Россия")
                .isActive(true)
                .build();

        countryRepository.createCountry(countryToDelete);

        mockMvc.perform(delete("/api/v1/country/delete/{id}", "RU"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("RU")))
                .andExpect(jsonPath("$.name", is("Россия")));

        Optional<Country> deletedCountry = countryRepository.findById("RU");
        assertThat(deletedCountry).isPresent();
        assertThat(deletedCountry.get().getName()).isEqualTo("Россия");
        assertThat(deletedCountry.get().isActive()).isFalse();

    }

    @Test
    void getCountryById_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/country/{id}", "RU"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Country not found with id: RU")));
    }

}

