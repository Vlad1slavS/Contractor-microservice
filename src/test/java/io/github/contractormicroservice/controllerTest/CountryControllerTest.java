package io.github.contractormicroservice.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.controller.CountryController;
import io.github.contractormicroservice.exception.EntityNotFoundException;
import io.github.contractormicroservice.exception.GlobalExceptionHandler;
import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.service.CountryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CountryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CountryServiceImpl countryServiceImpl;

    @InjectMocks
    private CountryController countryController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(countryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    /**
     * Тест для получения списка всех активных стран
     */
    @Test
    public void getAllCountries_ShouldReturnAllActiveCountries() throws Exception {

        List<CountryDTO> countries = Arrays.asList(
                CountryDTO.builder().id("RU").name("Россия").build(),
                CountryDTO.builder().id("CN").name("Китай").build(),
                CountryDTO.builder().id("DE").name("Германия").build()
        );

        when(countryServiceImpl.getAllActive()).thenReturn(countries);

        mockMvc.perform(get("/api/v1/country/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is("RU")))
                .andExpect(jsonPath("$[0].name", is("Россия")))
                .andExpect(jsonPath("$[1].id", is("CN")))
                .andExpect(jsonPath("$[1].name", is("Китай")))
                .andExpect(jsonPath("$[2].id", is("DE")))
                .andExpect(jsonPath("$[2].name", is("Германия")));

        verify(countryServiceImpl, times(1)).getAllActive();
    }

    /**
     * Тест поиска существующей страны по id
     */
    @Test
    public void getCountryById_ShouldReturnCountryById() throws Exception {

        CountryDTO countryDTO = CountryDTO.builder()
                .id("RU")
                .name("Россия")
                .build();

        when(countryServiceImpl.getOne("RU")).thenReturn(countryDTO);

        mockMvc.perform(get("/api/v1/country/{id}", "RU"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("RU")))
                .andExpect(jsonPath("$.name", is("Россия")));

        verify(countryServiceImpl, times(1)).getOne("RU");
    }

    /**
     * Тест поиска несуществующей страны по id
     * Теперь обрабатывается через @ControllerAdvice
     */
    @Test
    void getCountryById_ShouldReturn404_WhenNotFound() throws Exception {

        when(countryServiceImpl.getOne("XX"))
                .thenThrow(new EntityNotFoundException("Country not found with id: XX"));

        mockMvc.perform(get("/api/v1/country/{id}", "XX"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Сущность не найдена")))
                .andExpect(jsonPath("$.message", is("Country not found with id: XX")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.path", notNullValue()));

        verify(countryServiceImpl, times(1)).getOne("XX");
    }

    /**
     * Тест создания новой страны с валидными данными
     */
    @Test
    void saveCountry_ShouldCreateNewCountry_WhenValid() throws Exception {

        CountryDTO inputCountry = CountryDTO.builder()
                .id("FR")
                .name("Франция")
                .build();

        when(countryServiceImpl.save(any(CountryDTO.class))).thenReturn(inputCountry);

        mockMvc.perform(put("/api/v1/country/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCountry)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("FR")))
                .andExpect(jsonPath("$.name", is("Франция")));

        verify(countryServiceImpl, times(1)).save(any(CountryDTO.class));
    }

    /**
     * Тест валидации - null ID
     */
    @Test
    void saveCountry_ShouldReturn400_WhenIdIsEmpty() throws Exception {

        CountryDTO invalidCountry = CountryDTO.builder()
                .id(null)
                .name("Франция")
                .build();

        mockMvc.perform(put("/api/v1/country/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCountry)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации")))
                .andExpect(jsonPath("$.message", is("Переданные данные не прошли валидацию")))
                .andExpect(jsonPath("$.validationErrors.id", is("ID страны не может быть пустым")))
                .andExpect(jsonPath("$.status", is(400)));

        verify(countryServiceImpl, never()).save(any(CountryDTO.class));
    }

    /**
     * Тест валидации - пустое имя
     */
    @Test
    void saveCountry_ShouldReturn400_WhenNameIsEmpty() throws Exception {

        CountryDTO invalidCountry = CountryDTO.builder()
                .id("FR")
                .name("")
                .build();

        mockMvc.perform(put("/api/v1/country/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCountry)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации")))
                .andExpect(jsonPath("$.validationErrors.name", is("Название страны не может быть пустым")))
                .andExpect(jsonPath("$.status", is(400)));

        verify(countryServiceImpl, never()).save(any(CountryDTO.class));
    }

    /**
     * Тест валидации - слишком длинный ID
     */
    @Test
    void saveCountry_ShouldReturn400_WhenIdTooLong() throws Exception {

        CountryDTO invalidCountry = CountryDTO.builder()
                .id("FRFRFR") // 51 символ - превышает максимум
                .name("Франция")
                .build();

        mockMvc.perform(put("/api/v1/country/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCountry)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации")))
                .andExpect(jsonPath("$.validationErrors.id", is("ID страны должен содержать от 1 до 5 букв")))
                .andExpect(jsonPath("$.status", is(400)));

        verify(countryServiceImpl, never()).save(any(CountryDTO.class));
    }

    /**
     * Тест валидации - null значения
     */
    @Test
    void saveCountry_ShouldReturn400_WhenFieldsAreNull() throws Exception {

        CountryDTO invalidCountry = CountryDTO.builder()
                .id(null)
                .name(null)
                .build();

        mockMvc.perform(put("/api/v1/country/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCountry)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации")))
                .andExpect(jsonPath("$.validationErrors.id", is("ID страны не может быть пустым")))
                .andExpect(jsonPath("$.validationErrors.name", is("Название страны не может быть пустым")))
                .andExpect(jsonPath("$.status", is(400)));

        verify(countryServiceImpl, never()).save(any(CountryDTO.class));
    }

    /**
     * Тест логического удаления страны по id
     */
    @Test
    public void deleteCountryById_ShouldDeleteCountryById() throws Exception {

        CountryDTO deletedCountry = CountryDTO.builder()
                .id("RU")
                .name("Россия")
                .build();

        when(countryServiceImpl.deleteOne("RU")).thenReturn(deletedCountry);

        mockMvc.perform(delete("/api/v1/country/delete/{id}", "RU"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("RU")))
                .andExpect(jsonPath("$.name", is("Россия")));

        verify(countryServiceImpl, times(1)).deleteOne("RU");
    }

    /**
     * Тест удаления несуществующей страны
     */
    @Test
    void deleteCountryById_ShouldReturn404_WhenNotFound() throws Exception {

        when(countryServiceImpl.deleteOne("XX"))
                .thenThrow(new EntityNotFoundException("Country not found with id: XX"));

        mockMvc.perform(delete("/api/v1/country/delete/{id}", "XX"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Сущность не найдена")))
                .andExpect(jsonPath("$.message", is("Country not found with id: XX")))
                .andExpect(jsonPath("$.status", is(404)));

        verify(countryServiceImpl, times(1)).deleteOne("XX");
    }
}