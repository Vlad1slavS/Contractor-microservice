package io.github.contractormicroservice.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.controller.OrgFormController;
import io.github.contractormicroservice.exception.EntityNotFoundException;
import io.github.contractormicroservice.exception.GlobalExceptionHandler;
import io.github.contractormicroservice.model.dto.OrgFormDTO;
import io.github.contractormicroservice.service.OrgFormServiceImpl;
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
public class OrgFormControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrgFormServiceImpl orgFormService;

    /**
     * Экземпляр контроллера (с инжектом сервиса и валидатора (@Mock))
     */
    @InjectMocks
    private OrgFormController orgFormController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orgFormController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    /**
     * Тест для получения списка всех активных орг. форм
     * @throws Exception
     */
    @Test
    public void getAllOrgForms_ShouldReturnAllActiveOrgForms() throws Exception {

        List<OrgFormDTO> orgForms = Arrays.asList(
                OrgFormDTO.builder().id(1L).name("Нотариус").build(),
                OrgFormDTO.builder().id(2L).name("Адвокат").build(),
                OrgFormDTO.builder().id(3L).name("Ассоциация (союз)").build()
        );

        when(orgFormService.getAllActive()).thenReturn(orgForms);

        mockMvc.perform(get("/api/v1/orgForm/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Нотариус")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Адвокат")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].name", is("Ассоциация (союз)")));

        verify(orgFormService, times(1)).getAllActive();
    }

    /**
     * Тест поиска существующей орг. формы по id
     * @throws Exception
     */
    @Test
    public void getOrgFormById_ShouldReturnOrgFormById() throws Exception {

        OrgFormDTO orgFormDTO = OrgFormDTO.builder()
                .id(1L)
                .name("Нотариус")
                .build();

        when(orgFormService.getOne(1L)).thenReturn(orgFormDTO);

        mockMvc.perform(get("/api/v1/orgForm/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Нотариус")));

        verify(orgFormService, times(1)).getOne(1L);
    }

    /**
     * Тест поиска несуществующей огр. формы по id
     * @throws Exception
     */
    @Test
    void getOrgFormById_ShouldReturn404_WhenNotFound() throws Exception {

        when(orgFormService.getOne(1000L))
                .thenThrow(new EntityNotFoundException("Org form not found with id: " + 1000));

        mockMvc.perform(get("/api/v1/orgForm/{id}", 1000))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Сущность не найдена")))
                .andExpect(jsonPath("$.message", is("Org form not found with id: 1000")))
                .andExpect(jsonPath("$.status", is(404)));

        verify(orgFormService, times(1)).getOne(1000L);
    }

    /**
     * Тест создания новой орг. формы
     * @throws Exception
     */
    @Test
    void saveOrgForm_ShouldCreateNewOrgForm_WhenValid() throws Exception {

        OrgFormDTO inputOrgForm = OrgFormDTO.builder()
                .id(1L)
                .name("Нотариус")
                .build();

        when(orgFormService.save(any(OrgFormDTO.class))).thenReturn(inputOrgForm);

        mockMvc.perform(put("/api/v1/orgForm/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrgForm)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Нотариус")));

        verify(orgFormService, times(1))
                .save(any(OrgFormDTO.class));
    }

    /**
     * Тест логического удаления орг формы по id
     * @throws Exception
     */
    @Test
    public void deleteIndustryById_ShouldDeleteIndustryById() throws Exception {

        OrgFormDTO deletedOrgForm = OrgFormDTO.builder()
                .id(1L)
                .name("Нотариус")
                .build();

        when(orgFormService.deleteOne(1L)).thenReturn(deletedOrgForm);

        mockMvc.perform(delete("/api/v1/orgForm/delete/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Нотариус")));

        verify(orgFormService, times(1)).deleteOne(1L);
    }

    /**
     * Тест валидации - пустое название
     */
    @Test
    void saveOrgForm_ShouldReturn400_WhenNameIsEmpty() throws Exception {

        OrgFormDTO invalidOrgForm = OrgFormDTO.builder()
                .id(1L)
                .name("")
                .build();

        mockMvc.perform(put("/api/v1/orgForm/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrgForm)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации")))
                .andExpect(jsonPath("$.message", is("Переданные данные не прошли валидацию")))
                .andExpect(jsonPath("$.validationErrors.name", is("Название организационной формы не может быть пустым")))
                .andExpect(jsonPath("$.status", is(400)));

        verify(orgFormService, never()).save(any(OrgFormDTO.class));
    }


}

