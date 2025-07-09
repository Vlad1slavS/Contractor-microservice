package io.github.contractormicroservice.repository.contractor;

import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.dto.IndustryDTO;
import io.github.contractormicroservice.model.dto.OrgFormDTO;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.model.entity.Pagination;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.HashMap;

public class ContractorJdbcRepositoryImpl implements ContractorJdbcRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ContractorJdbcRepositoryImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private final RowMapper<Contractor> contractorRowMapper = (rs, rowNum) -> {
        Contractor contractor = new Contractor();
        contractor.setId(rs.getString("id"));
        contractor.setParentId(rs.getString("parent_id"));
        contractor.setName(rs.getString("name"));
        contractor.setNameFull(rs.getString("name_full"));
        contractor.setInn(rs.getString("inn"));
        contractor.setOgrn(rs.getString("ogrn"));
        String country = rs.getString("country");
        contractor.setCountry(rs.wasNull() ? null : country);
        Long industry = rs.getLong("industry");
        contractor.setIndustry(rs.wasNull() ? null : industry);
        Long orgForm = rs.getLong("org_form");
        contractor.setOrgForm(rs.wasNull() ? null : orgForm);
        contractor.setCreateDate(rs.getTimestamp("create_date") != null ?
                rs.getTimestamp("create_date").toLocalDateTime() : null);
        contractor.setModifyDate(rs.getTimestamp("modify_date") != null ?
                rs.getTimestamp("modify_date").toLocalDateTime() : null);
        contractor.setCreateUserId(rs.getString("create_user_id"));
        contractor.setModifyUserId(rs.getString("modify_user_id"));
        contractor.setIsActive(rs.getBoolean("is_active"));

        if (rs.getString("country_name") != null) {
            CountryDTO countryInfo = new CountryDTO();
            countryInfo.setId(rs.getString("country"));
            countryInfo.setName(rs.getString("country_name"));
            contractor.setCountryEntity(countryInfo);
        }


        if (rs.getObject("industry_id") != null) {
            IndustryDTO industryInfo = new IndustryDTO();
            industryInfo.setId(rs.getLong("industry_id"));
            industryInfo.setName(rs.getString("industry_name"));
            contractor.setIndustryEntity(industryInfo);
        }

        if (rs.getObject("org_form_id") != null) {
            OrgFormDTO orgFormInfo = new OrgFormDTO();
            orgFormInfo.setId(rs.getLong("org_form_id"));
            orgFormInfo.setName(rs.getString("org_form_name"));
            contractor.setOrgFormEntity(orgFormInfo);
        }

        return contractor;
    };

    @Override
    public Optional<Contractor> findByIdWithDetails(String id) {
        String sql = """
        SELECT c.id, c.parent_id, c.name, c.name_full, c.inn, c.ogrn,
            c.country, c.industry, c.org_form,
            c.create_date, c.modify_date, c.create_user_id, c.modify_user_id, c.is_active,
            co.name as country_name, co.is_active as country_is_active,
            i.id as industry_id, i.name as industry_name, i.is_active as industry_is_active,
            of.id as org_form_id, of.name as org_form_name, of.is_active as org_form_is_active
        FROM contractor c
        LEFT JOIN country co ON c.country = co.id
        LEFT JOIN industry i ON c.industry = i.id
        LEFT JOIN org_form of ON c.org_form = of.id
        WHERE c.id = :id
        """;

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);

        List<Contractor> contractors = namedParameterJdbcTemplate.query(sql, parameterSource, contractorRowMapper);

        return contractors.isEmpty() ? Optional.empty() : Optional.of(contractors.getFirst());
    }

    @Override
    public Pagination searchContractors(ContractorFilter request, Integer page, Integer limit) {

        Map<String, Object> parameterSource = new HashMap<>();
        parameterSource.put("limit", limit);
        parameterSource.put("offset", page * limit);

        String sql = """
            SELECT COUNT(*) FROM contractor c
            LEFT JOIN country co ON c.country = co.id
            LEFT JOIN industry i ON c.industry = i.id
            LEFT JOIN org_form of ON c.org_form = of.id
            WHERE c.is_active = true
        """;

        Long count = namedParameterJdbcTemplate.queryForObject(sql, parameterSource, Long.class);

        if (count == null) {
            return new Pagination(Collections.emptyList(), page, limit, 0);
        }

        String filters = searchFilters(request, parameterSource);

        String searchSql = """
            SELECT c.id, c.parent_id, c.name, c.name_full, c.inn, c.ogrn,
                c.country, c.industry, c.org_form,
                c.create_date, c.modify_date, c.create_user_id, c.modify_user_id, c.is_active,
                co.name as country_name, co.is_active as country_is_active,
                i.id as industry_id, i.name as industry_name, i.is_active as industry_is_active,
                of.id as org_form_id, of.name as org_form_name, of.is_active as org_form_is_active
            FROM contractor c
            LEFT JOIN country co ON c.country = co.id
            LEFT JOIN industry i ON c.industry = i.id
            LEFT JOIN org_form of ON c.org_form = of.id

            WHERE c.is_active = true""" + filters + """

            ORDER BY c.id
            LIMIT :limit OFFSET :offset
            """;

        List<Contractor> contractors = namedParameterJdbcTemplate.query(searchSql, parameterSource, contractorRowMapper);

        return new Pagination(contractors, page, limit, count.intValue());


    }

    private String searchFilters(ContractorFilter request, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (request == null) {
            return sql.toString();
        }

        if (request.getContractorId() != null && !request.getContractorId().isEmpty()) {
            sql.append(" AND c.id = :contractor_id");
            params.put("contractor_id", request.getContractorId());
        }

        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            sql.append(" AND c.parent_id = :parent_id");
            params.put("parent_id", request.getParentId());
        }

        if (request.getContractorSearch() != null && !request.getContractorSearch().trim().isEmpty()) {
            sql.append(" AND (c.name ILIKE :search OR c.name_full ILIKE :search OR c.inn ILIKE :search OR c.ogrn ILIKE :search)");
            params.put("search", "%" + request.getContractorSearch().trim() + "%");
        }


        if (request.getCountry() != null && !request.getCountry().isEmpty()) {
            sql.append(" AND c.country = :country_name");
            params.put("country_name", request.getCountry());
        }

        if (request.getIndustry() != null && request.getIndustry() != 0) {
            sql.append(" AND c.industry = :industry");
            params.put("industry", request.getIndustry());
        }

        if (request.getOrgForm() != null && !request.getOrgForm().isEmpty()) {
            sql.append(" AND c.org_form = :org_form_name");
            params.put("org_form_name", request.getOrgForm());
        }

        return sql.toString();

    }

}
