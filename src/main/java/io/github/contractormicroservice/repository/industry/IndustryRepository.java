package io.github.contractormicroservice.repository.industry;

import io.github.contractormicroservice.model.entity.Industry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD репозиторий для работы с индустриальными кодами
 */
@Repository
public interface IndustryRepository extends CrudRepository<Industry, Long>, IndustryJdbcRepository {
}
