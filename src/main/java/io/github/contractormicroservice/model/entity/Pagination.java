package io.github.contractormicroservice.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Pagination {

    private List<Contractor> contractors;

    private Integer page;

    private Integer limit;

    private Integer totalElements;

    private Boolean hasNext;

    private Boolean hasPrevious;

    public Pagination(List<Contractor> contractors, Integer page, Integer limit, Integer totalElements) {
        this.contractors = contractors;
        this.hasNext = (page * limit) < totalElements;
        this.hasPrevious = page > 1;
        this.page = page;
        this.limit = limit;
        this.totalElements = totalElements;
    }

}
