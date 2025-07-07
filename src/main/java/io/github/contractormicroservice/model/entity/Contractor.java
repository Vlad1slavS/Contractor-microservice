package io.github.contractormicroservice.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * Класс модели контрагента
 */
@Data
@NoArgsConstructor
public class Contractor {

    @Id
    private String id;
    private String parentId;
    private String name;
    private String nameFull;
    private String inn;
    private String ogrn;
    private String country;
    private Integer industry;
    private Integer orgForm;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private String createUserId;
    private String modifyUserId;

    private Boolean isActive = true;

    private Country countryEntity;
    private Industry industryEntity;
    private OrgForm orgFormEntity;

    public Contractor(String id, String parentId, String name, String nameFull,
                      String inn, String ogrn, String country, Integer industry,
                      Integer orgForm, LocalDateTime createDate, LocalDateTime modifyDate,
                      String createUserId, String modifyUserId) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.nameFull = nameFull;
        this.inn = inn;
        this.ogrn = ogrn;
        this.country = country;
        this.industry = industry;
        this.orgForm = orgForm;
        this.createDate = createDate;
        this.modifyDate = modifyDate;
        this.createUserId = createUserId;
        this.modifyUserId = modifyUserId;
        this.isActive = true;
    }

}
