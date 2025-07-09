package io.github.contractormicroservice.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContractorFilter {

    @JsonProperty("contractor_id")
    private String contractorId;

    @JsonProperty("parent_id")
    private String parentId;

    @JsonProperty("contractor_search")
    private String contractorSearch;

    private String country;
    private Integer industry;

    @JsonProperty("org_form")
    private String orgForm;

}
