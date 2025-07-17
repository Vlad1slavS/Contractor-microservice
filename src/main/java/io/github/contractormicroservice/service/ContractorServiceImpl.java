package io.github.contractormicroservice.service;

import io.github.contractormicroservice.exception.EntityNotFoundException;
import io.github.contractormicroservice.model.dto.ContractorDTO;
import io.github.contractormicroservice.model.entity.Contractor;
import io.github.contractormicroservice.model.entity.ContractorFilter;
import io.github.contractormicroservice.model.entity.Pagination;
import io.github.contractormicroservice.repository.contractor.ContractorRepository;
import io.github.contractormicroservice.repository.country.CountryRepository;
import io.github.contractormicroservice.repository.industry.IndustryRepository;
import io.github.contractormicroservice.repository.orgForm.OrgFormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ContractorServiceImpl implements ContractorService {

    private final ContractorRepository contractorRepository;
    private final CountryRepository countryRepository;
    private final IndustryRepository industryRepository;
    private final OrgFormRepository orgFormRepository;

    public ContractorServiceImpl(ContractorRepository contractorRepository,
                                 CountryRepository countryRepository,
                                 IndustryRepository industryRepository,
                                 OrgFormRepository orgFormRepository) {
        this.contractorRepository = contractorRepository;
        this.countryRepository = countryRepository;
        this.industryRepository = industryRepository;
        this.orgFormRepository = orgFormRepository;
    }

    public Contractor getOne(String id) {
        return contractorRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Contractor not found with id: " + id));
    }

    public Contractor deleteOne(String id) {
        Contractor contractor = contractorRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Contractor not found with id: " + id));
        contractor.setIsActive(false);
        return contractorRepository.save(contractor);
    }

    public Contractor save(ContractorDTO contractorDTO) {

        Optional<Contractor> contractor = contractorRepository.findByIdWithDetails(contractorDTO.getId());

        Contractor newContractor;

        if (contractor.isPresent()) {
            newContractor = contractor.get();
            newContractor.setName(sanitize(contractorDTO.getName()));
            newContractor.setParentId(sanitize(contractorDTO.getParentId()));
            newContractor.setNameFull(sanitize(contractorDTO.getNameFull()));
            newContractor.setInn(sanitize(contractorDTO.getInn()));
            newContractor.setOgrn(sanitize(contractorDTO.getOgrn()));
            newContractor.setCountry(contractorDTO.getCountry());
            newContractor.setIndustry(contractorDTO.getIndustry());
            newContractor.setOrgForm(contractorDTO.getOrgForm());
            newContractor.setModifyDate(LocalDateTime.now());
            newContractor.markAsExisting();
        } else {
            newContractor = Contractor.builder()
                    .id(contractorDTO.getId())
                    .parentId(sanitize(contractorDTO.getParentId()))
                    .name(sanitize(contractorDTO.getName()))
                    .nameFull(sanitize(contractorDTO.getNameFull()))
                    .inn(sanitize(contractorDTO.getInn()))
                    .ogrn(sanitize(contractorDTO.getOgrn()))
                    .country(contractorDTO.getCountry())
                    .industry(contractorDTO.getIndustry())
                    .orgForm(contractorDTO.getOrgForm())
                    .createDate(LocalDateTime.now())
                    .modifyDate(LocalDateTime.now())
                    .build();

            newContractor.markAsNew();
        }

        validateFK(newContractor);

        return contractorRepository.save(newContractor);

    }

    /**
     * Метод для проверки на пустую строку (если строка пустая, то возвращаем null для вставки в БД)
     * @param value - проверяемая строка
     * @return результат
     */
    private String sanitize(String value) {
        return StringUtils.hasText(value) ? value : null;
    }

    @Transactional
    public Pagination searchContractors(ContractorFilter searchRequest, Integer page, Integer limit) {

        if (page < 0) {
            page = 0;
        }

        if (limit < 0 || limit > 100) {
            limit = 10;
        }

        return contractorRepository.searchContractors(searchRequest, page, limit);
    }

    /**
     * Валидация существования связанных сущностей при сохранении
     * @param contractor - входная сущность
     */
    private void validateFK(Contractor contractor) {
        if (contractor.getCountry() != null && !contractor.getCountry().isEmpty()) {
            if (!countryRepository.existsById(contractor.getCountry())) {
                throw new EntityNotFoundException("Country not found with id: " + contractor.getCountry());
            }
        }

        if (contractor.getIndustry() != null) {
            if (!industryRepository.existsById(contractor.getIndustry())) {
                throw new EntityNotFoundException("Industry not found with id: " + contractor.getIndustry());
            }
        }

        if (contractor.getOrgForm() != null) {
            if (!orgFormRepository.existsById(contractor.getOrgForm())) {
                throw new EntityNotFoundException("Org form not found with id: " + contractor.getOrgForm());
            }
        }

    }

}

