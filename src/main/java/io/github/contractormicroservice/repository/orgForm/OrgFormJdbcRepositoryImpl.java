package io.github.contractormicroservice.repository.orgForm;

import io.github.contractormicroservice.model.entity.OrgForm;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class OrgFormJdbcRepositoryImpl implements OrgFormJdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public OrgFormJdbcRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<OrgForm> orgFormRowMapper = (rs, rowNum) -> {
        OrgForm orgForm = new OrgForm();
        orgForm.setId(rs.getLong("id"));
        orgForm.setName(rs.getString("name"));
        return orgForm;
    };

    @Override
    public List<OrgForm> findAllActive() {
        String sql = "SELECT * FROM org_form WHERE is_active = true";
        return namedParameterJdbcTemplate.query(sql, orgFormRowMapper);
    }

    @Override
    @Transactional
    public OrgForm createOrgForm(OrgForm orgForm) {
        String sql = "INSERT INTO org_form (name, is_active) VALUES (:name, :isActive) RETURNING id, name";

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", orgForm.getName())
                .addValue("isActive", orgForm.isActive());

        return namedParameterJdbcTemplate.queryForObject(sql, parameterSource, orgFormRowMapper);
    }

    @Override
    @Transactional
    public void synchronizeSequence() {
        String sql = "SELECT setval('org_form_id_seq', COALESCE((SELECT MAX(id) FROM org_form), 0) + 1, false)";
        namedParameterJdbcTemplate.execute(sql, (PreparedStatement ps) -> {
            ps.execute();
            return null;
        });
    }

}
