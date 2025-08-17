package io.github.contractormicroservice.service;

import io.github.contractormicroservice.model.dto.OrgFormDTO;

import java.util.List;

/**
 * Интерфейс сервиса для работы с организационными формами
 */
public interface OrgFormService {

    /**
     * Получить все активные организационные формы
     * @return список организационных форм
     */
    List<OrgFormDTO> getAllActive();

    /**
     * Получить организационную форму по ID
     * @param id идентификатор организационной формы
     * @return организационная форма
     */
    OrgFormDTO getOne(Long id);

    /**
     * Логически удалить организационную форму
     * @param id идентификатор организационной формы
     * @return удаленная организационная форма
     */
    OrgFormDTO deleteOne(Long id);

    /**
     * Сохранить или обновить организационную форму
     * @param countryDTO данные организационной формы
     * @return сохраненная организационная форма
     */
    OrgFormDTO save(OrgFormDTO countryDTO);

}
