package io.github.contractormicroservice.service;

import io.github.contractormicroservice.model.dto.CountryDTO;

import java.util.List;

/**
 * Интерфейс сервиса для работы со странами
 */
public interface CountryService {

    /**
     * Получить все активные страны
     * @return список активных стран
     */
    List<CountryDTO> getAllActive();

    /**
     * Получить страну по ID
     * @param id идентификатор страны
     * @return страна
     */
    CountryDTO getOne(String id);

    /**
     * Логически удалить страну
     * @param id идентификатор страны
     * @return удаленная страна
     */
    CountryDTO deleteOne(String id);

    /**
     * Сохранить или обновить страну
     * @param countryDTO данные страны
     * @return сохраненная страна
     */
    CountryDTO save(CountryDTO countryDTO);
}
