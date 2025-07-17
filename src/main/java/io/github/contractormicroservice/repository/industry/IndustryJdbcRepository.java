package io.github.contractormicroservice.repository.industry;

import io.github.contractormicroservice.model.entity.Industry;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Расширение методов CrudRepository для работы с БД
 */
@Repository
public interface IndustryJdbcRepository {

    List<Industry> findAllActive();

    void synchronizeSequence();

}
