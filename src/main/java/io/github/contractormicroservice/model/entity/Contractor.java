package io.github.contractormicroservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.dto.IndustryDTO;
import io.github.contractormicroservice.model.dto.OrgFormDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * Класс модели контрагента (со связанными сущностями)
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Contractor implements Persistable<String> {

    @Id
    private String id;
    private String parentId;
    private String name;
    private String nameFull;
    private String inn;
    private String ogrn;
    private String country;
    private Long industry;
    private Long orgForm;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    private String createUserId;
    private String modifyUserId;

    @Builder.Default
    @JsonIgnore
    private Boolean isActive = true;

    @Transient
    private CountryDTO countryEntity;
    @Transient
    private IndustryDTO industryEntity;
    @Transient
    private OrgFormDTO orgFormEntity;

    @Transient
    @JsonIgnore
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markAsNew() {
        this.isNew = true;
    }

    public void markAsExisting() {
        this.isNew = false;
    }

}

