package io.github.contractormicroservice.controllerTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.controller.IndustryController;
import io.github.contractormicroservice.exception.EntityNotFoundException;
import io.github.contractormicroservice.exception.GlobalExceptionHandler;
import io.github.contractormicroservice.model.dto.IndustryDTO;
import io.github.contractormicroservice.service.IndustryServiceImpl;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class IndustryControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IndustryServiceImpl industryServiceImpl;

    /**
     * Экземпляр контроллера (с инжектом сервиса и валидатора (@Mock))
     */
    @InjectMocks
    private IndustryController industryController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(industryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    /**
     * Тест для получения списка всех активных индустриальных кодов
     * @throws Exception
     */
    @Test
    public void getAllIndustries_ShouldReturnAllActiveIndustries() throws Exception {

        List<IndustryDTO> industries = Arrays.asList(
                IndustryDTO.builder().id(1L).name("Энергетика").build(),
                IndustryDTO.builder().id(2L).name("Авиастроение").build(),
                IndustryDTO.builder().id(3L).name("Судостроение").build()
        );

        when(industryServiceImpl.getAllActive()).thenReturn(industries);

        mockMvc.perform(get("/api/v1/industries/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Энергетика")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Авиастроение")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].name", is("Судостроение")));

        verify(industryServiceImpl, times(1)).getAllActive();
    }

    /**
     * Тест поиска существующего индустриального кода по id
     * @throws Exception
     */
    @Test
    public void getIndustryById_ShouldReturnIndustryById() throws Exception {

        IndustryDTO industryDTO = IndustryDTO.builder()
                .id(1L)
                .name("Авиастроение")
                .build();

        when(industryServiceImpl.getOne(1L)).thenReturn(industryDTO);

        mockMvc.perform(get("/api/v1/industries/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Авиастроение")));

        verify(industryServiceImpl, times(1)).getOne(1L);
    }

    /**
     * Тест поиска несуществующего индустриального кода по id
     * @throws Exception
     */
    @Test
    void getIndustryById_ShouldReturn404_WhenNotFound() throws Exception {

        when(industryServiceImpl.getOne(999L))
                .thenThrow(new EntityNotFoundException("Industry not found with id: 999"));

        mockMvc.perform(get("/api/v1/industries/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Сущность не найдена")))
                .andExpect(jsonPath("$.message", is("Industry not found with id: 999")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.path", notNullValue()));

        verify(industryServiceImpl, times(1)).getOne(999L);
    }

    /**
     * Тест создания нового индустриального кода
     * @throws Exception
     */
    @Test
    void saveIndustry_ShouldCreateNewIndustry_WhenValid() throws Exception {

        IndustryDTO inputIndustry = IndustryDTO.builder()
                .id(1L)
                .name("Авиастроение")
                .build();

        when(industryServiceImpl.save(any(IndustryDTO.class))).thenReturn(inputIndustry);

        mockMvc.perform(put("/api/v1/industries/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputIndustry)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Авиастроение")));

        verify(industryServiceImpl, times(1))
                .save(any(IndustryDTO.class));
    }

    /**
     * Тест логического удаления индустриального кода по id
     * @throws Exception
     */
    @Test
    public void deleteIndustryById_ShouldDeleteIndustryById() throws Exception {

        IndustryDTO deletedIndustry = IndustryDTO.builder()
                .id(1L)
                .name("Авиастроение")
                .build();

        when(industryServiceImpl.deleteOne(1L)).thenReturn(deletedIndustry);

        mockMvc.perform(delete("/api/v1/industries/delete/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Авиастроение")));

            verify(industryServiceImpl, times(1)).deleteOne(1L);
    }


    /**
     * Тест валидации - пустое название
     */
    @Test
    void saveIndustry_ShouldReturn400_WhenNameIsEmpty() throws Exception {

        IndustryDTO invalidIndustry = IndustryDTO.builder()
                .id(1L)
                .name("")
                .build();

        mockMvc.perform(put("/api/v1/industries/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidIndustry)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации")))
                .andExpect(jsonPath("$.message", is("Переданные данные не прошли валидацию")))
                .andExpect(jsonPath("$.validationErrors.name", is("Название индустриального кода не может быть пустым")))
                .andExpect(jsonPath("$.status", is(400)));

        verify(industryServiceImpl, never()).save(any(IndustryDTO.class));
    }

    /**
     * Тест валидации - null ID
     */
    @Test
    void saveIndustry_ShouldReturn400_WhenIdIsNull() throws Exception {

        IndustryDTO invalidIndustry = IndustryDTO.builder()
                .id(null)
                .name("Авиастроение")
                .build();

        mockMvc.perform(put("/api/v1/industries/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidIndustry)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации")))
                .andExpect(jsonPath("$.validationErrors.id", is("ID индустриального кода не может быть null")))
                .andExpect(jsonPath("$.status", is(400)));

        verify(industryServiceImpl, never()).save(any(IndustryDTO.class));
    }

}
