package io.github.contractormicroservice.repository.orgForm;

import io.github.contractormicroservice.model.entity.OrgForm;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrgFormRepository extends CrudRepository<OrgForm, Long>, OrgFormJdbcRepository {
}
