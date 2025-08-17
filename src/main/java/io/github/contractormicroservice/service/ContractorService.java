package io.github.contractormicroservice.service;

import io.github.contractormicroservice.model.dto.ContractorDTO;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.model.entity.Pagination;

/**
 * Интерфейс сервиса для работы со странами
 */
public interface ContractorService {

    /**
     * Получить контрагента по id
     * @param id идентификатор контрагента
     * @return контрагент
     */
    Contractor getOne(String id);

    /**
     * Логически удалить контрагента
     * @param id идентификатор контрагента
     * @return удаленный контрагент
     */
    Contractor deleteOne(String id);

    /**
     * Сохранить или обновить контрагента
     * @param contractorDTO данные контрагента
     * @return сохраненный контрагент
     */
    Contractor save(ContractorDTO contractorDTO);

    /**
     * Сохранить или обновить контрагента
     * @param searchRequest - фильтр поиска
     * @param page - номер страницы
     * @param limit - количество контрагентов на странице
     * @return сохраненный контрагент
     */
    Pagination searchContractors(ContractorFilter searchRequest, Integer page, Integer limit);

}
