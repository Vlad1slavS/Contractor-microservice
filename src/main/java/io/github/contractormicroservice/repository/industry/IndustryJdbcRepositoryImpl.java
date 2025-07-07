package io.github.contractormicroservice.repository.industry;

import io.github.contractormicroservice.model.entity.Industry;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Реализация интерфейса IndustryJdbcRepository
 */
@Repository
public class IndustryJdbcRepositoryImpl implements IndustryJdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public IndustryJdbcRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<Industry> industryRowMapper = (rs, rowNum) -> {
        Industry industry = new Industry();
        industry.setId(rs.getLong("id"));
        industry.setName(rs.getString("name"));
        return industry;
    };

    @Override
    public List<Industry> findAllActive() {
        String sql = "SELECT * FROM industry WHERE is_active = true";
        return namedParameterJdbcTemplate.query(sql, industryRowMapper);
    }

    @Override
    @Transactional
    public Industry createIndustry(Industry industry) {
        String sql = "INSERT INTO industry (name, is_active) VALUES (:name, :isActive) RETURNING id, name";

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", industry.getName())
                .addValue("isActive", industry.isActive());

        return namedParameterJdbcTemplate.queryForObject(sql, parameterSource, industryRowMapper);
    }

    @Override
    @Transactional
    public void synchronizeSequence() {
        String sql = "SELECT setval('industry_id_seq', COALESCE((SELECT MAX(id) FROM industry), 0) + 1, false)";
        namedParameterJdbcTemplate.execute(sql, (PreparedStatement ps) -> {
            ps.execute();
            return null;
        });
    }

}

