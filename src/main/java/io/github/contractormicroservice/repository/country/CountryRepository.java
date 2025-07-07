package io.github.contractormicroservice.repository.country;

import io.github.contractormicroservice.model.entity.Country;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD репозиторий для Country (базовые операции: save, findBtId и т.д)
 */
@Repository
public interface CountryRepository extends CrudRepository<Country, String>, CountryJdbcRepository {
}



