package io.github.contractormicroservice.repository.country;

import io.github.contractormicroservice.model.entity.Country;

import java.util.List;

/**
 * Расширение методов CrudRepository для работы с БД
 */
public interface CountryJdbcRepository {

    List<Country> findAllActive();

}
