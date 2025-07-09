package io.github.contractormicroservice.repository.contractor;

import io.github.contractormicroservice.model.entity.Contractor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractorRepository extends CrudRepository<Contractor, String>, ContractorJdbcRepository {

}
