package io.github.contractormicroservice.service;

import io.github.contractormicroservice.model.dto.IndustryDTO;

import java.util.List;

/**
 * Интерфейс сервиса для работы с индустриальными кодами
 */
public interface IndustryService {

    /**
     * Получить все активные индустриальные коды
     * @return список активных индустриальных кодов
     */
    List<IndustryDTO> getAllActive();

    /**
     * Получить индустриальный код по ID
     * @param id идентификатор индустриального кода
     * @return индустриальный код
     */
    IndustryDTO getOne(Long id);

    /**
     * Логическое удаление индустриального кода
     * @param id идентификатор индустриального кода
     * @return удаленный индустриальный код
     */
    IndustryDTO deleteOne(Long id);

    /**
     * Сохранить или обновить индустриальный код
     * @param industryDTO данные индустриального кода
     * @return сохраненный индустриальный код
     */
    IndustryDTO save(IndustryDTO industryDTO);

}
