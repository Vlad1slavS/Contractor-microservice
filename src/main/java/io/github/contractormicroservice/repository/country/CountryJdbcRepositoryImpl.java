package io.github.contractormicroservice.repository.country;

import io.github.contractormicroservice.model.entity.Country;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Реализация класса CountryJdbcRepository
 */
@Repository
public class CountryJdbcRepositoryImpl implements CountryJdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CountryJdbcRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<Country> countryRowMapper = (rs, rowNum) -> {
        Country country = new Country();
        country.setId(rs.getString("id"));
        country.setName(rs.getString("name"));
        return country;
    };

    @Override
    public List<Country> findAllActive() {
        String sql = "SELECT * FROM country WHERE is_active = true";
        return namedParameterJdbcTemplate.query(sql, countryRowMapper);
    }

}


