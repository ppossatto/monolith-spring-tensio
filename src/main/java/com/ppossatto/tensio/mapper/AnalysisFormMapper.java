package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.diagnosis.Diagnosis;
import com.ppossatto.tensio.entity.AnalysisEntity;
import com.ppossatto.tensio.entity.DiagnosisEntity;
import com.ppossatto.tensio.form.AnalysisForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/// MapStruct mapper for registering analyses: form → analysis entity and computed diagnosis → entity.
/// Relations (transformer, analysis) are set by the service.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnalysisFormMapper {

  /// Copies the form values into an analysis entity.
  ///
  /// @param form   the validated form
  /// @param entity the entity to fill
  @Mapping(target = "transformer", ignore = true)
  void update(AnalysisForm form, @MappingTarget AnalysisEntity entity);

  /// Copies a computed diagnosis into a diagnosis entity.
  ///
  /// @param diagnosis the diagnosis
  /// @param entity    the entity to fill
  @Mapping(target = "analysis", ignore = true)
  void update(Diagnosis diagnosis, @MappingTarget DiagnosisEntity entity);
}
