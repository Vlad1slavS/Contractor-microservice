package io.github.contractormicroservice.service;

import io.github.auditlib.annotation.AuditLog;
import io.github.contractormicroservice.exception.EntityNotFoundException;
import io.github.contractormicroservice.model.dto.IndustryDTO;
import io.github.contractormicroservice.model.entity.Industry;
import io.github.contractormicroservice.repository.industry.IndustryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с индустриальными кодами (бизнес-логика)
 */
@Service
public class IndustryServiceImpl implements IndustryService {

    private final IndustryRepository industryRepository;

    public IndustryServiceImpl(IndustryRepository industryRepository) {
        this.industryRepository = industryRepository;
    }

    public List<IndustryDTO> getAllActive() {
        List<Industry> industries = industryRepository.findAllActive();
        return IndustryDTO.fromEntityList(industries);
    }

    public IndustryDTO getOne(Long id) {
        Industry industry = industryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Industry not found with id: " + id));
        return IndustryDTO.fromEntity(industry);
    }

    public IndustryDTO deleteOne(Long id) {
        Industry industry = industryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Industry not found with id: " + id));
        industry.setActive(false);
        industryRepository.save(industry);
        return IndustryDTO.fromEntity(industry);
    }

    @AuditLog(logLevel = AuditLog.LogLevel.DEBUG)
    public IndustryDTO save(IndustryDTO industryDTO) {

        if (industryDTO.getId() != null) {
            Optional<Industry> existingIndustry = industryRepository.findById(industryDTO.getId());

            if (existingIndustry.isPresent()) {
                existingIndustry.get().setName(industryDTO.getName());
                industryRepository.save(existingIndustry.get());
                return IndustryDTO.fromEntity(existingIndustry.get());
            } else {
                throw new EntityNotFoundException("Industry not found with id: " + industryDTO.getId());
            }
        }
        Industry newIndustry = Industry.builder()
                .name(industryDTO.getName())
                .build();
        Industry savedIndustry = industryRepository.save(newIndustry);
        return IndustryDTO.fromEntity(savedIndustry);
    }

}