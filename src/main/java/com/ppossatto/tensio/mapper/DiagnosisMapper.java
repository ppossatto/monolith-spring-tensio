package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.diagnosis.LatestDiagnosis;
import com.ppossatto.tensio.entity.TransformerLatestDiagnosis;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/// MapStruct mapper from `TransformerLatestDiagnosis` to the `LatestDiagnosis` domain type.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DiagnosisMapper {

  /// Converts an entity to its domain type.
  ///
  /// @param entity the entity
  /// @return the domain object
  LatestDiagnosis toDomain(TransformerLatestDiagnosis entity);
}
