package io.github.contractormicroservice.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.contractormicroservice.controller.ContractorController;
import io.github.contractormicroservice.exception.EntityNotFoundException;
import io.github.contractormicroservice.exception.GlobalExceptionHandler;
import io.github.contractormicroservice.model.dto.ContractorDTO;
import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.dto.IndustryDTO;
import io.github.contractormicroservice.model.dto.OrgFormDTO;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.model.entity.Pagination;
import io.github.contractormicroservice.service.ContractorService;
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

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ContractorControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ContractorService contractorService;

    @InjectMocks
    private ContractorController contractorController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(contractorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(new LocalValidatorFactoryBean())
                .build();
    }

    @Test
    public void getContractorById_ShouldReturnContractor() throws Exception {

        CountryDTO country = CountryDTO.builder()
                .id("TEST_ID")
                .name("TEST_NAME")
                .build();

        Contractor expectedContractor = Contractor.builder()
                .id("TEST_ID")
                .name("TEST_NAME")
                .countryEntity(country)
                .build();

        when(contractorService.getOne(expectedContractor.getId())).thenReturn(expectedContractor);

        mockMvc.perform(get("/api/v1/contractor/{id}", expectedContractor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedContractor.getId()))
                .andExpect(jsonPath("$.name").value(expectedContractor.getName()))
                .andExpect(jsonPath("$.countryEntity.id").value(expectedContractor.getCountryEntity().getId()))
                .andExpect(jsonPath("$.countryEntity.name").value(expectedContractor.getCountryEntity().getName()));

        verify(contractorService, times(1)).getOne(expectedContractor.getId());
    }

    /**
     * Тест поиска контрагента по неверному id
     * @throws Exception
     */
    @Test
    void getContractorById_ShouldReturn404() throws Exception {

        when(contractorService.getOne("XX"))
                .thenThrow(new EntityNotFoundException("Contractor not found with id: " + "XX"));

        mockMvc.perform(get("/api/v1/contractor/{id}", "XX"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Сущность не найдена")))
                .andExpect(jsonPath("$.message", is("Contractor not found with id: XX")))
                .andExpect(jsonPath("$.status", is(404)));

        verify(contractorService, times(1)).getOne("XX");
    }

    /**
     * Тест создания нового контрагента
     * @throws Exception
     */
    @Test
    void saveContractor_ShouldCreateNewContractor() throws Exception {

        ContractorDTO inputContractor = ContractorDTO.builder()
                .id("TEST_ID")
                .name("TEST_NAME")
                .country("RU")
                .industry(1L)
                .orgForm(1L)
                .build();

        Contractor expectedContractor = Contractor.builder()
                .id("TEST_ID")
                .name("TEST_NAME")
                .countryEntity(CountryDTO.builder().id("RU").name("Россия").build())
                .industryEntity(IndustryDTO.builder().id(1L).name("TEST_NAME").build())
                .orgFormEntity(OrgFormDTO.builder().id(1L).name("TEST_NAME").build())
                .build();

        when(contractorService.save(inputContractor)).thenReturn(expectedContractor);

        mockMvc.perform(put("/api/v1/contractor/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputContractor)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedContractor.getId()))
                .andExpect(jsonPath("$.name").value(expectedContractor.getName()))
                .andExpect(jsonPath("$.countryEntity.id").value(expectedContractor.getCountryEntity().getId()))
                .andExpect(jsonPath("$.countryEntity.name").value(expectedContractor.getCountryEntity().getName()))
                .andExpect(jsonPath("$.industryEntity.name").value(expectedContractor.getIndustryEntity().getName()))
                .andExpect(jsonPath("$.orgFormEntity.name").value(expectedContractor.getOrgFormEntity().getName()));

        verify(contractorService, times(1))
                .save(any(ContractorDTO.class));
    }

    /**
     * Тест поиска контрагентов с пагинацией
     * @throws Exception
     */
    @Test
    void searchContractors_Success() throws Exception {

        Contractor testContractor = Contractor.builder()
                .id("TEST_ID")
                .name("TEST_NAME")
                .build();

        Pagination testPagination = new Pagination();
        testPagination.setContractors(List.of(testContractor));
        testPagination.setTotalElements(1);
        testPagination.setPage(0);

        when(contractorService.searchContractors(any(), eq(0), eq(10)))
                .thenReturn(testPagination);

        mockMvc.perform(post("/api/v1/contractor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractors").isArray())
                .andExpect(jsonPath("$.contractors[0].id").value("TEST_ID"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));

    }

    /**
     * Тест поиска контрагентов с пагинацией и фильтром
     * @throws Exception
     */
    @Test
    void searchContractorsWithFilter_Success() throws Exception {

        ContractorFilter request = new ContractorFilter();
        request.setCountry("TEST_COUNTRY");

        Contractor testContractor = Contractor.builder()
                .id("TEST_ID")
                .name("TEST_NAME")
                .country("TEST_COUNTRY")
                .build();

        Pagination testPagination = new Pagination();
        testPagination.setContractors(List.of(testContractor));
        testPagination.setTotalElements(1);
        testPagination.setPage(0);

        when(contractorService.searchContractors(any(), eq(0), eq(10)))
                .thenReturn(testPagination);

        mockMvc.perform(post("/api/v1/contractor/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .param("page", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractors").isArray())
                .andExpect(jsonPath("$.contractors[0].id").value("TEST_ID"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.contractors[0].country").value("TEST_COUNTRY"));

    }

}
