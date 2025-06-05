package com.google.clusterfuzz.web.mapper;

import com.google.clusterfuzz.core.entity.Testcase;
import com.google.clusterfuzz.web.dto.TestcaseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for converting between Testcase entity and TestcaseDto.
 * Provides automatic mapping with custom configurations where needed.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TestcaseMapper {

    /**
     * Convert Testcase entity to TestcaseDto.
     */
    TestcaseDto toDto(Testcase testcase);

    /**
     * Convert TestcaseDto to Testcase entity.
     */
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    Testcase toEntity(TestcaseDto testcaseDto);

    /**
     * Update existing Testcase entity with values from TestcaseDto.
     * Useful for partial updates.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    void updateEntityFromDto(TestcaseDto testcaseDto, @MappingTarget Testcase testcase);
}