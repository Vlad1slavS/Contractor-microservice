package io.github.contractormicroservice.service;

import io.github.contractormicroservice.exception.EntityNotFoundException;
import io.github.contractormicroservice.model.dto.CountryDTO;
import io.github.contractormicroservice.model.entity.Country;
import io.github.contractormicroservice.repository.country.CountryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы со странами (бизнес-логика)
 */
@Service
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    public CountryServiceImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List<CountryDTO> getAllActive() {
        List<Country> countries = countryRepository.findAllActive();
        return CountryDTO.fromEntityList(countries);
    }

    public CountryDTO getOne(String id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + id));
        return CountryDTO.fromEntity(country);
    }

    public CountryDTO deleteOne(String id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Country not found with id: " + id));
        country.setActive(false);
        countryRepository.save(country);
        return CountryDTO.fromEntity(country);
    }

    public CountryDTO save(CountryDTO countryDTO) {

        Optional<Country> country = countryRepository.findById(countryDTO.getId());

        if (country.isPresent()) {
            country.get().setName(countryDTO.getName());
            countryRepository.save(country.get());
            return CountryDTO.fromEntity(country.get());
        } else {
            Country newCountry = Country.builder()
                    .id(countryDTO.getId())
                    .name(countryDTO.getName())
                    .build();
            countryRepository.createCountry(newCountry);
            return CountryDTO.fromEntity(newCountry);
        }

    }

}


