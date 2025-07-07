package io.github.contractormicroservice.config;

import io.github.contractormicroservice.repository.industry.IndustryRepository;
import io.github.contractormicroservice.repository.orgForm.OrgFormRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Класс для синхронизации sequences (для вставки новых записей с правильными id)
 */
@Slf4j
@Component
public class DatabaseConfig {

    private final IndustryRepository industryRepository;
    private final OrgFormRepository orgFormRepository;

    public DatabaseConfig(IndustryRepository industryRepository, OrgFormRepository orgFormRepository) {
        this.industryRepository = industryRepository;
        this.orgFormRepository = orgFormRepository;
    }

    @PostConstruct
    public void init() {
        try {
            log.info("Начало синхронизации sequences...");

            industryRepository.synchronizeSequence();
            log.info("Industry sequence синхронизирован");

            orgFormRepository.synchronizeSequence();
            log.info("OrgForm sequence синхронизирован");

        } catch (Exception e) {
            log.error("Ошибка при синхронизации sequences: {}", e.getMessage());
        }
    }

}

