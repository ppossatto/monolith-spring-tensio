package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.analysis.Analysis;
import com.ppossatto.tensio.entity.AnalysisEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/// MapStruct mapper from `AnalysisEntity` to the `Analysis` domain type.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnalysisMapper {

  /// Converts an entity to its domain type.
  ///
  /// @param entity the entity
  /// @return the domain object
  @Mapping(target = "transformerId", source = "transformer.id")
  Analysis toDomain(AnalysisEntity entity);
}
