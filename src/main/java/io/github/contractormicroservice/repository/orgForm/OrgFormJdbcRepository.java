package io.github.contractormicroservice.repository.orgForm;

import io.github.contractormicroservice.model.entity.OrgForm;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgFormJdbcRepository {

    List<OrgForm> findAllActive();

    OrgForm createOrgForm(OrgForm orgForm);

    void synchronizeSequence();

}
